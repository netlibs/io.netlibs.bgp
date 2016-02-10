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
 * File: org.bgp4j.netty.fsm.FireEventTimeManager.java
 */
package io.joss.bgp.netty.fsm;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.EverythingMatcher;
import org.quartz.listeners.TriggerListenerSupport;

import lombok.RequiredArgsConstructor;

/**
 * @author Rainer Bieniek (Rainer.Bieniek@web.de)
 *
 */

@RequiredArgsConstructor
class FireEventTimeManager<T extends FireEventTimeJob>
{

  private class ClearTriggerData extends TriggerListenerSupport
  {
    private final String name = UUID.randomUUID().toString();

    @Override
    public String getName()
    {
      return this.name;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.quartz.listeners.TriggerListenerSupport#triggerComplete(org.quartz.Trigger, org.quartz.JobExecutionContext,
     * org.quartz.Trigger.CompletedExecutionInstruction)
     */
    @Override
    public void triggerComplete(final Trigger trigger,
        final JobExecutionContext context,
        final CompletedExecutionInstruction triggerInstructionCode)
    {
      if (trigger.getKey().equals(FireEventTimeManager.this.triggerKey))
      {
        FireEventTimeManager.this.firedWhen = null;
        FireEventTimeManager.this.triggerKey = null;
      }
    }
  }

  private final ClearTriggerData clearTriggerData = new ClearTriggerData();

  private final Scheduler scheduler;
  private JobDetail jobDetail;
  private TriggerKey triggerKey;
  private JobKey jobKey;
  private Date firedWhen;

  void createJobDetail(final Class<T> jobClass, final InternalFSM fsm, final Map<String, Object> additionalJobData) throws SchedulerException
  {
    final JobDataMap map = new JobDataMap();

    map.put(FireEventTimeJob.FSM_KEY, fsm);

    if (additionalJobData != null)
    {
      for (final Entry<String, Object> entry : additionalJobData.entrySet())
      {
        map.put(entry.getKey(), entry.getValue());
      }
    }

    this.jobKey = new JobKey(UUID.randomUUID().toString());
    this.jobDetail = JobBuilder.newJob(jobClass).usingJobData(map).withIdentity(this.jobKey).build();

    this.scheduler.getListenerManager().addTriggerListener(this.clearTriggerData, EverythingMatcher.allTriggers());
  }

  void createJobDetail(final Class<T> jobClass, final InternalFSM fsm) throws SchedulerException
  {
    this.createJobDetail(jobClass, fsm, null);
  }

  void shutdown() throws SchedulerException
  {
    this.cancelJob();

    this.scheduler.getListenerManager().removeTriggerListener(this.clearTriggerData.getName());
  };

  synchronized void scheduleJob(final int whenInSeconds) throws SchedulerException
  {
    this.triggerKey = TriggerKey.triggerKey(UUID.randomUUID().toString());

    if (this.scheduler.checkExists(this.jobKey))
    {
      this.firedWhen = this.scheduler.scheduleJob(TriggerBuilder.newTrigger()
          .withIdentity(this.triggerKey)
          .forJob(this.jobKey)
          .withSchedule(SimpleScheduleBuilder.simpleSchedule())
          .startAt(new Date(System.currentTimeMillis() + (whenInSeconds * 1000L)))
          .build());
    }
    else
    {
      this.firedWhen = this.scheduler.scheduleJob(this.jobDetail, TriggerBuilder.newTrigger()
          .withIdentity(this.triggerKey)
          .withSchedule(SimpleScheduleBuilder.simpleSchedule())
          .startAt(new Date(System.currentTimeMillis() + (whenInSeconds * 1000L)))
          .build());
    }
  }

  synchronized boolean isJobScheduled() throws SchedulerException
  {
    if (this.triggerKey == null)
    {
      return false;
    }
    return this.scheduler.checkExists(this.triggerKey);
  }

  synchronized void cancelJob() throws SchedulerException
  {
    if (this.triggerKey != null)
    {
      this.scheduler.unscheduleJob(this.triggerKey);
      this.triggerKey = null;
      this.firedWhen = null;
    }
  }

  /**
   * @return the firedWhen
   */
  synchronized Date getFiredWhen()
  {
    return this.firedWhen;
  }
}
