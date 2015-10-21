/**
 *
 */
package org.bgp4j.rib.processor;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.bgp4j.config.nodes.RoutingInstanceConfiguration;
import org.bgp4j.config.nodes.RoutingProcessorConfiguration;
import org.slf4j.Logger;

/**
 * @author rainer
 *
 */
public class RoutingProcessor
{
  private @Inject Instance<RoutingInstance> instanceProvider;
  private @Inject Logger log;

  private List<RoutingInstance> instances = new LinkedList<RoutingInstance>();

  public void configure(final RoutingProcessorConfiguration configuration)
  {
    for (final RoutingInstanceConfiguration instConfig : configuration.getRoutingInstances())
    {
      final RoutingInstance instance = this.instanceProvider.get();

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
