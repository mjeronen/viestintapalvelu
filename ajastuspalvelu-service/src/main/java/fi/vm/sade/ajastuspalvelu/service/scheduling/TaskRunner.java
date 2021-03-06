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

import fi.vm.sade.ajastuspalvelu.service.scheduling.dto.ErrorDto;
import fi.vm.sade.ajastuspalvelu.service.scheduling.dto.ScheduledTaskExecutionDetailsDto;
import fi.vm.sade.ajastuspalvelu.service.scheduling.dto.TaskResultDto;

/**
 * Determines a business process
 *
 * User: ratamaa
 * Date: 23.10.2014
 * Time: 13:55
 */
public interface TaskRunner {

    /**
     * Run this processes by given scheduledTask
     *
     * @param scheduledTask the task
     * @throws Exception
     */
    TaskResultDto run(ScheduledTaskExecutionDetailsDto scheduledTask) throws Exception;

    /**
     * Called if the run method trow an exception to handle and translate the exception
     * with proper cause and message
     *
     * @param scheduledTask the the task with when
     * @param error the cause of the error
     * @return the translated cause of the error with possible message or the original error
     */
    ErrorDto handleError(ScheduledTaskExecutionDetailsDto scheduledTask, ErrorDto error);

}
