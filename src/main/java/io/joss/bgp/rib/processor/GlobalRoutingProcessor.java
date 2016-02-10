/**
 * 
 */
package io.joss.bgp.rib.processor;

import java.util.List;

import io.joss.bgp.config.global.ApplicationConfiguration;
import lombok.RequiredArgsConstructor;

/**
 * @author rainer
 *
 */

@RequiredArgsConstructor
public class GlobalRoutingProcessor
{

  private final ApplicationConfiguration appConfig;
  private final RoutingProcessor routingProcessor;

  /**
   * @param configuration
   * @see io.joss.bgp.rib.processor.RoutingProcessor#configure(org.bgp4j.config.nodes.RoutingProcessorConfiguration)
   */
  public void configure()
  {
    if (this.appConfig.getRoutingProcessorConfiguration() != null)
    {
      this.routingProcessor.configure(this.appConfig.getRoutingProcessorConfiguration());
    }
  }

  /**
   * 
   * @see io.joss.bgp.rib.processor.RoutingProcessor#startService()
   */
  public void startService()
  {
    this.routingProcessor.startService();
  }

  /**
   * 
   * @see io.joss.bgp.rib.processor.RoutingProcessor#stopService()
   */
  public void stopService()
  {
    this.routingProcessor.stopService();
  }

  /**
   * @return
   * @see io.joss.bgp.rib.processor.RoutingProcessor#getInstances()
   */
  public List<RoutingInstance> getInstances()
  {
    return this.routingProcessor.getInstances();
  }
}
