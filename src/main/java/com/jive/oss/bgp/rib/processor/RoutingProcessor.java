/**
 *
 */
package com.jive.oss.bgp.rib.processor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;

import com.jive.oss.bgp.config.nodes.RoutingInstanceConfiguration;
import com.jive.oss.bgp.config.nodes.RoutingProcessorConfiguration;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author rainer
 *
 */

@Slf4j
@RequiredArgsConstructor
public class RoutingProcessor
{
  
  private  RoutingInstance instance;

  private List<RoutingInstance> instances = new LinkedList<RoutingInstance>();

  public void configure(final RoutingProcessorConfiguration configuration)
  {
    for (final RoutingInstanceConfiguration instConfig : configuration.getRoutingInstances())
    {
      instance.configure(instConfig);
      this.instances.add(instance);
    }

    this.instances = Collections.unmodifiableList(this.instances);
  }

  public void startService()
  {
    for (final RoutingInstance instance : this.instances)
    {
      this.log.info("Starting routing instance between " + instance.getFirstPeerName() + " and " + instance.getSecondPeerName());

      try
      {
        instance.startInstance();

        this.log.info("Starting routing instance between " + instance.getFirstPeerName() + " and " + instance.getSecondPeerName() + " in state " + instance.getState());
      }
      catch (final Throwable t)
      {
        this.log.error("failed to start routing instances between " + instance.getFirstPeerName() + " and " + instance.getSecondPeerName(), t);
      }
    }
  }

  public void stopService()
  {
    for (final RoutingInstance instance : this.instances)
    {
      this.log.info("Stopping routing instance between " + instance.getFirstPeerName() + " and " + instance.getSecondPeerName());

      try
      {
        instance.stopInstance();
      }
      catch (final Throwable t)
      {
        this.log.error("failed to stop routing instances between " + instance.getFirstPeerName() + " and " + instance.getSecondPeerName(), t);
      }
    }
  }

  /**
   * @return the instances
   */
  public List<RoutingInstance> getInstances()
  {
    return this.instances;
  }

}
