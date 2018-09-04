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
package fi.vm.sade.viestintapalvelu.externalinterface.api;

import fi.vm.sade.ryhmasahkoposti.api.dto.EmailData;
import org.apache.http.HttpResponse;

import java.io.IOException;

public interface EmailResource {

    /**
     * Lähettää ryhmäsähköpostin vastaanottajille ilman alaviitettä
     *
     * @param emailData
     *            Lähetettävän ryhmäsähköpostin tiedot
     * @return Lähetettävän ryhmäsähköpostiviestin tunnus
     */

    HttpResponse sendEmail(EmailData emailData) throws IOException;

    HttpResponse getPreview(EmailData emailData) throws Exception;
}
