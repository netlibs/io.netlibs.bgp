/**
 *  Copyright 2012 Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 * File: org.bgp4j.netty.fsm.OutboundRoutingUpdateQueue.java
 */
package io.joss.bgp.netty.fsm;

import java.net.Inet4Address;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

import io.joss.bgp.net.AddressFamily;
import io.joss.bgp.net.AddressFamilyKey;
import io.joss.bgp.net.BinaryNextHop;
import io.joss.bgp.net.InetAddressNextHop;
import io.joss.bgp.net.NetworkLayerReachabilityInformation;
import io.joss.bgp.net.RIBSide;
import io.joss.bgp.net.SubsequentAddressFamily;
import io.joss.bgp.net.attributes.MultiProtocolReachableNLRI;
import io.joss.bgp.net.attributes.MultiProtocolUnreachableNLRI;
import io.joss.bgp.net.attributes.NextHopPathAttribute;
import io.joss.bgp.net.attributes.PathAttribute;
import io.joss.bgp.netty.BGPv4Constants;
import io.joss.bgp.netty.NLRICodec;
import io.joss.bgp.netty.protocol.update.UpdatePacket;
import io.joss.bgp.rib.Route;
import io.joss.bgp.rib.RouteAdded;
import io.joss.bgp.rib.RouteWithdrawn;
import io.joss.bgp.rib.RoutingEventListener;
import io.joss.bgp.rib.RoutingInformationBaseVisitor;
import io.joss.bgp.rib.TopologicalTreeSortingKey;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

public class OutboundRoutingUpdateQueue implements RoutingEventListener
{

  public static class BatchJob implements Job
  {

    public static final String CALLBACK_KEY = "Callback";
    public static final String QUEUE_KEY = "Queue";

    @Override
    public void execute(final JobExecutionContext context) throws JobExecutionException
    {
      ((OutboundRoutingUpdateCallback) context.getMergedJobDataMap().get(CALLBACK_KEY))
      .sendUpdates(((OutboundRoutingUpdateQueue) context.getMergedJobDataMap().get(QUEUE_KEY)).buildUpdates());
    }

  }

  private class QueueingVisitor implements RoutingInformationBaseVisitor
  {

    @Override
    public void visitRouteNode(final String ribName, final RIBSide side, final Route route)
    {
      OutboundRoutingUpdateQueue.this.addRoute(ribName, side, route);
    }

  }

  private OutboundRoutingUpdateCallback callback;
  private String peerName;
  private Set<AddressFamilyKey> updateMask;
  private boolean active;
  private final Map<TopologicalTreeSortingKey, List<NetworkLayerReachabilityInformation>> addedRoutes = new TreeMap<TopologicalTreeSortingKey, List<NetworkLayerReachabilityInformation>>();
  private final Map<AddressFamilyKey, List<NetworkLayerReachabilityInformation>> withdrawnRoutes = new TreeMap<AddressFamilyKey, List<NetworkLayerReachabilityInformation>>();
  private final Scheduler scheduler;
  private JobDetail jobDetail;
  private TriggerKey triggerKey;
  private JobKey jobKey;

  public OutboundRoutingUpdateQueue(final Scheduler scheduler)
  {
    this.scheduler = scheduler;
  }

  RoutingInformationBaseVisitor getImportVisitor()
  {
    return new QueueingVisitor();
  }

  @Override
  public void routeAdded(final RouteAdded event)
  {
    if (this.active && (event.getSide() == RIBSide.Local) && StringUtils.equals(event.getPeerName(), this.peerName)
        && this.updateMask.contains(event.getRoute().getAddressFamilyKey()))
    {
      this.addRoute(this.peerName, event.getSide(), event.getRoute());
    }
  }

  @Override
  public void routeWithdrawn(final RouteWithdrawn event)
  {
    if (this.active && (event.getSide() == RIBSide.Local) && StringUtils.equals(event.getPeerName(), this.peerName)
        && this.updateMask.contains(event.getRoute().getAddressFamilyKey()))
    {
      this.withdrawRoute(this.peerName, event.getSide(), event.getRoute());
    }
  }

  /**
   * @return the peerName
   */
  String getPeerName()
  {
    return this.peerName;
  }

  /**
   * @param peerName
   *          the peerName to set
   */
  void setPeerName(final String peerName)
  {
    this.peerName = peerName;
  }

  /**
   * @return the updateMask
   */
  Set<AddressFamilyKey> getUpdateMask()
  {
    return this.updateMask;
  }

  /**
   * @param updateMask
   *          the updateMask to set
   */
  void setUpdateMask(final Set<AddressFamilyKey> updateMask)
  {
    this.updateMask = updateMask;
  }

  /**
   * @return the active
   */
  boolean isActive()
  {
    return this.active;
  }

  void shutdown() throws SchedulerException
  {
    this.active = false;
    this.cancelJob();
    synchronized (this.addedRoutes)
    {
      this.addedRoutes.clear();
    }
  };

  void startSendingUpdates(final int repeatInterval) throws SchedulerException
  {
    if (repeatInterval > 0)
    {

      if (this.isJobScheduled())
      {
        this.cancelJob();
      }

      final JobDataMap map = new JobDataMap();

      map.put(BatchJob.CALLBACK_KEY, this.callback);
      map.put(BatchJob.QUEUE_KEY, this);

      this.jobKey = new JobKey(UUID.randomUUID().toString());
      this.jobDetail = JobBuilder.newJob(BatchJob.class).usingJobData(map).withIdentity(this.jobKey).build();
      this.triggerKey = TriggerKey.triggerKey(UUID.randomUUID().toString());

      this.scheduler.scheduleJob(this.jobDetail, TriggerBuilder.newTrigger()
          .withIdentity(this.triggerKey)
          .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(repeatInterval))
          .startAt(new Date(System.currentTimeMillis() + (repeatInterval * 1000L)))
          .build());
    }
    this.active = true;
  }

  /**
   * @param callback
   *          the callback to set
   */
  void setCallback(final OutboundRoutingUpdateCallback callback)
  {
    this.callback = callback;
  }

  @SuppressWarnings("unchecked")
  private void addRoute(final String ribName, final RIBSide side, final Route route)
  {

    TopologicalTreeSortingKey key;
    Collection<PathAttribute> keyAttributes;

    if (route.getAddressFamilyKey().matches(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_UNICAST_FORWARDING))
    {
      // handle non-MP IPv4 case
      keyAttributes = this.filterAttribute(route.getPathAttributes(), Arrays.asList(NextHopPathAttribute.class));
      keyAttributes.add(new NextHopPathAttribute((InetAddressNextHop<Inet4Address>) route.getNextHop()));
    }
    else
    {

      // handle any other case
      keyAttributes = this.filterAttribute(route.getPathAttributes(), Arrays.asList(MultiProtocolReachableNLRI.class, MultiProtocolReachableNLRI.class));

      keyAttributes.add(
          new MultiProtocolReachableNLRI(
              route.getAddressFamilyKey().getAddressFamily(),
              route.getAddressFamilyKey().getSubsequentAddressFamily(),
              (BinaryNextHop) route.getNextHop()));

    }

    key = new TopologicalTreeSortingKey(route.getAddressFamilyKey(), keyAttributes);

    synchronized (this.addedRoutes)
    {

      if (!this.addedRoutes.containsKey(key))
      {
        this.addedRoutes.put(key, new LinkedList<NetworkLayerReachabilityInformation>());
      }

      this.addedRoutes.get(key).add(route.getNlri());

    }

  }

  private void withdrawRoute(final String ribName, final RIBSide side, final Route route)
  {
    // remove the NLRI from any scheduled route add updates
    synchronized (this.addedRoutes)
    {
      final Set<TopologicalTreeSortingKey> removeableKeys = new HashSet<TopologicalTreeSortingKey>();

      for (final TopologicalTreeSortingKey key : this.addedRoutes.keySet())
      {
        if (key.getAddressFamilyKey().equals(route.getAddressFamilyKey()))
        {
          this.addedRoutes.get(key).remove(route.getNlri());

          if (this.addedRoutes.get(key).size() == 0)
          {
            removeableKeys.add(key);
          }
        }
      }

      for (final TopologicalTreeSortingKey key : removeableKeys)
      {
        this.addedRoutes.remove(key);
      }
    }

    synchronized (this.withdrawnRoutes)
    {
      if (!this.withdrawnRoutes.containsKey(route.getAddressFamilyKey()))
      {
        this.withdrawnRoutes.put(route.getAddressFamilyKey(), new LinkedList<NetworkLayerReachabilityInformation>());
      }

      this.withdrawnRoutes.get(route.getAddressFamilyKey()).add(route.getNlri());
    }
  }

  private Collection<PathAttribute> filterAttribute(final Collection<PathAttribute> source,
      final Collection<? extends Class<? extends PathAttribute>> filteredClasses)
  {
    final LinkedList<PathAttribute> result = new LinkedList<PathAttribute>();

    for (final PathAttribute pa : source)
    {
      if (!filteredClasses.contains(pa.getClass()))
      {
        result.add(pa);
      }
    }

    return result;
  }

  List<UpdatePacket> buildUpdates()
  {
    final List<UpdatePacket> updates = new LinkedList<UpdatePacket>();
    UpdatePacket current = null;

    synchronized (this.withdrawnRoutes)
    {
      for (final Entry<AddressFamilyKey, List<NetworkLayerReachabilityInformation>> withdrawnRouteEntry : this.withdrawnRoutes.entrySet())
      {
        for (final NetworkLayerReachabilityInformation nlri : withdrawnRouteEntry.getValue())
        {
          MultiProtocolUnreachableNLRI mpUnreachable = null;
          final AddressFamilyKey afk = withdrawnRouteEntry.getKey();

          if (((current == null)
              || ((current.calculatePacketSize() + NLRICodec.calculateEncodedNLRILength(nlri)) > (BGPv4Constants.BGP_PACKET_MAX_LENGTH - BGPv4Constants.BGP_PACKET_HEADER_LENGTH))))
          {
            current = new UpdatePacket();

            if (!afk.equals(AddressFamilyKey.IPV4_UNICAST_FORWARDING))
            {
              mpUnreachable = new MultiProtocolUnreachableNLRI(afk.getAddressFamily(), afk.getSubsequentAddressFamily());
              current.getPathAttributes().add(mpUnreachable);
            }
            updates.add(current);
          }

          if (afk.matches(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_UNICAST_FORWARDING))
          {
            current.getWithdrawnRoutes().add(nlri);
          }
          else
          {
            mpUnreachable.getNlris().add(nlri);
          }
        }
        current = null;
      }

      this.withdrawnRoutes.clear();
    }

    synchronized (this.addedRoutes)
    {

      for (final Entry<TopologicalTreeSortingKey, List<NetworkLayerReachabilityInformation>> addedRouteEntry : this.addedRoutes.entrySet())
      {

        final TopologicalTreeSortingKey key = addedRouteEntry.getKey();
        final List<NetworkLayerReachabilityInformation> nlris = addedRouteEntry.getValue();
        MultiProtocolReachableNLRI mpNLRI = null;

        if (!key.getAddressFamilyKey().matches(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_UNICAST_FORWARDING))
        {
          for (final PathAttribute pathAttribute : key.getPathAttributes())
          {
            if (pathAttribute instanceof MultiProtocolReachableNLRI)
            {
              mpNLRI = (MultiProtocolReachableNLRI) pathAttribute;
              break;
            }
          }
        }

        for (final NetworkLayerReachabilityInformation nlri : nlris)
        {
          if (((current == null)
              || ((current.calculatePacketSize() + NLRICodec.calculateEncodedNLRILength(nlri)) > (BGPv4Constants.BGP_PACKET_MAX_LENGTH - BGPv4Constants.BGP_PACKET_HEADER_LENGTH))))
          {
            current = new UpdatePacket();
            current.getPathAttributes().addAll(key.getPathAttributes());
            updates.add(current);
          }


          if (key.getAddressFamilyKey().matches(AddressFamily.IPv4, SubsequentAddressFamily.NLRI_UNICAST_FORWARDING))
          {
            current.getNlris().add(nlri);
          }
          else
          {
            mpNLRI.getNlris().add(nlri);
          }
        }

        current = null;
      }

      this.addedRoutes.clear();
    }

    return updates;
  }

  int getNumberOfPendingUpdates()
  {

    int result = 0;

    synchronized (this.addedRoutes)
    {
      result += this.addedRoutes.size();
    }

    synchronized (this.withdrawnRoutes)
    {
      result += this.withdrawnRoutes.size();
    }

    return result;
  }

  boolean isJobScheduled() throws SchedulerException
  {

    if (this.triggerKey == null)
    {
      return false;
    }

    return this.scheduler.checkExists(this.triggerKey);

  }

  public Date getNextFireWhen() throws SchedulerException
  {
    if (!this.isJobScheduled())
    {
      return null;
    }

    return this.scheduler.getTrigger(this.triggerKey).getFireTimeAfter(new Date(System.currentTimeMillis()));
  }

  void cancelJob() throws SchedulerException
  {
    if (this.triggerKey != null)
    {
      this.scheduler.unscheduleJob(this.triggerKey);
      this.triggerKey = null;
    }
  }

}