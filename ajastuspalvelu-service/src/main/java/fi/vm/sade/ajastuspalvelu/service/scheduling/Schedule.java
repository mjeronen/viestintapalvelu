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

import java.io.Serializable;

import org.joda.time.DateTime;

import com.google.common.base.Optional;

/**
 * User: ratamaa
 * Date: 23.10.2014
 * Time: 12:50
 */
public interface Schedule extends Serializable {

    /**
     * @return true if this schedule is valid to be run at some point in the future (if false, may be ignored)
     */
    boolean isValid();

    /**
     * @return a valid cron expression
     */
    String getCron();

    /**
     * @return active period begin
     */
    Optional<DateTime> getActiveBegin();

    /**
     * @return active period end
     */
    Optional<DateTime> getActiveEnd();

}
