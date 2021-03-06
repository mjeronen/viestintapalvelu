/**
 * Copyright (c) 2014 The Finnish Board of Education - Opetushallitus
 *
 * This program is free software:  Licensed under the EUPL, Version 1.1 or - as
 * soon as they will be approved by the European Commission - subsequent versions
 * of the EUPL (the "Licence");
 *
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at: http://www.osor.eu/eupl/
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * European Union Public Licence for more details.
 **/
package fi.vm.sade.ryhmasahkoposti.util;

import fi.vm.sade.generic.model.BaseEntity;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * User: ratamaa
 * Date: 15.9.2014
 * Time: 16:42
 */
public class UpdateVersionAnswer implements Answer<Void> {
    public Void answer(InvocationOnMock invocation) throws Throwable {
        BaseEntity entity = (BaseEntity) invocation.getArguments()[0];
        entity.setVersion(entity.getVersion()+1);
        return null;
    }
}
