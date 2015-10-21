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
 * File: org.bgp4j.netty.fsm.SchedulerFactory.java
 */
package org.bgp4j.netty.fsm;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

public class SchedulerFactory
{

  private final Scheduler scheduler;

  public SchedulerFactory() throws SchedulerException
  {
    final StdSchedulerFactory factory = new StdSchedulerFactory();

    factory.initialize();
    this.scheduler = factory.getScheduler();
  }

  @Produces
  Scheduler producerScheduler()
  {
    return this.scheduler;
  }

  public void startScheduler(@Observes final ApplicationBootstrapEvent event) throws SchedulerException
  {
    this.scheduler.start();
  }

  public void stopScheduler(@Observes final ApplicationShutdownEvent event) throws SchedulerException
  {
    this.scheduler.shutdown();
  }

}
