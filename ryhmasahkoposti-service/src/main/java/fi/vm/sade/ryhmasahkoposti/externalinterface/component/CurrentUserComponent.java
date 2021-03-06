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
package fi.vm.sade.ryhmasahkoposti.externalinterface.component;

import java.util.List;

import fi.vm.sade.externalinterface.OppijanumeroRekisteriRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fi.vm.sade.dto.HenkiloDto;
import fi.vm.sade.dto.OrganisaatioHenkiloDto;
import fi.vm.sade.ryhmasahkoposti.util.SecurityUtil;
import fi.vm.sade.externalinterface.KayttooikeusRestClient;
import fi.vm.sade.viestintapalvelu.common.exception.ExternalInterfaceException;

/**
 * Komponenttiluokka omien tietojen hakemiseksi käyttäen CXF:ää {@link service-context.xml}
 * 
 * @author vehei1
 *
 */
@Component
public class CurrentUserComponent {
    private static Logger LOGGER = LoggerFactory.getLogger(CurrentUserComponent.class);
    private final KayttooikeusRestClient kayttooikeusRestClient;
    private final OppijanumeroRekisteriRestClient oppijanumeroRekisteriRestClient;

    @Autowired
    public CurrentUserComponent(KayttooikeusRestClient kayttooikeusRestClient, OppijanumeroRekisteriRestClient oppijanumeroRekisteriRestClient) {
        this.oppijanumeroRekisteriRestClient = oppijanumeroRekisteriRestClient;
        this.kayttooikeusRestClient = kayttooikeusRestClient;
    }


    /**
     * Hakee kirjaantuneen käyttäjän tiedot
     * 
     * @return Henkilon tiedot
     */
    public HenkiloDto getCurrentUser() {
        try {
            String oid = SecurityUtil.getOid();
            return oppijanumeroRekisteriRestClient.getHenkilo(oid);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new ExternalInterfaceException("error.msg.gettingCurrentUserFailed", e);
        }
    }

    /**
     * Hakee kirjautuneen käyttäjän organisaatioiden tiedot
     * 
     * @return Lista henkilön organisaatiotietoja
     */
    public List<OrganisaatioHenkiloDto> getCurrentUserOrganizations() {
        try {
            String oid = SecurityUtil.getOid();
            return kayttooikeusRestClient.getOrganisaatioHenkilo(oid);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            throw new ExternalInterfaceException("error.msg.gettingCurrentUserOrganizationFailed", e);
        }
    }
}
