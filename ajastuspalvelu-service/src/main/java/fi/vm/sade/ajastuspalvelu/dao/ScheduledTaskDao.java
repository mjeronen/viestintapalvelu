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

package fi.vm.sade.ajastuspalvelu.dao;

import java.util.List;

import fi.vm.sade.ajastuspalvelu.model.ScheduledTask;
import fi.vm.sade.ajastuspalvelu.service.dto.ScheduledTaskCriteriaDto;
import fi.vm.sade.generic.dao.JpaDAO;

/**
 * User: ratamaa
 * Date: 23.10.2014
 * Time: 13:37
 */
public interface ScheduledTaskDao extends JpaDAO<ScheduledTask, Long> {

    List<ScheduledTask> find(ScheduledTaskCriteria criteria);

    int count(ScheduledTaskCriteriaDto criteria);
}
