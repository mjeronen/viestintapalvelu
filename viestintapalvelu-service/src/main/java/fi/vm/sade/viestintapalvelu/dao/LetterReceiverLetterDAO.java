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
package fi.vm.sade.viestintapalvelu.dao;

import java.util.List;

import fi.vm.sade.generic.dao.JpaDAO;
import fi.vm.sade.viestintapalvelu.model.LetterReceiverLetter;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rajapinta vastaanottajien kirjeiden tietokantakäsittelyä varten
 * 
 * @author vehei1
 *
 */
public interface LetterReceiverLetterDAO extends JpaDAO<LetterReceiverLetter, Long> {

    /**
     * Hakee vastaanottajille lähetetyt kirjeet vastaanottajien tunnuksilla
     * 
     * @param letterReceiverIDs Lista vasttanottajien tunnuksia
     * @return Lista vastaanottajille lähetettyjä kirjeitä
     */
    List<LetterReceiverLetter> getLetterReceiverLettersByLetterReceiverIds(List<Long> letterReceiverIDs);

    @Transactional
    int markAsPublished(Long id);
}
