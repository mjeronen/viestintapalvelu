/*
 * Copyright (c) 2014 The Finnish National Board of Education - Opetushallitus
 *
 * This program is free software: Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * European Union Public Licence for more details.
 */

package fi.vm.sade.ajastuspalvelu.service.scheduling;

import org.quartz.SchedulerException;

/**
 * User: ratamaa
 * Date: 21.10.2014
 * Time: 16:50
 */
public interface QuartzSchedulingService {

    /**
     * Schedule a ScheduledTask or change it's schedule.
     *
     * @see fi.vm.sade.ajastuspalvelu.service.scheduling.impl.SingleScheduledTaskTaskSchedule
     *
     * @param scheduledTaskId id of a ScheduledTask
     * @param schedule the (new) schedule to use
     * @throws SchedulerException on Quartz scheduler error
     */
    void scheduleJob(Long scheduledTaskId, Schedule schedule) throws SchedulerException;

    /**
     * Call when scheduled task is removed / disabled.
     *
     * @param scheduledTaskId id of a ScheduledTask the unschedule
     * @throws SchedulerException on Quartz scheduler error
     */
    void unscheduleJob(Long scheduledTaskId) throws SchedulerException;

}
