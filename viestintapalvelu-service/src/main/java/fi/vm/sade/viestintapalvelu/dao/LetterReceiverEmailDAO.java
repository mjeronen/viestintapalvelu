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

import fi.vm.sade.dto.PagingAndSortingDTO;
import fi.vm.sade.generic.dao.JpaDAO;
import fi.vm.sade.viestintapalvelu.model.LetterReceiverEmail;

public interface LetterReceiverEmailDAO extends JpaDAO<LetterReceiverEmail, Long> {
    /**
     * Hakee vastaanottajien osoitetietoja vastaanottajien tunnuksilla
     * 
     * @param letterReceiverIDs
     *            Lista vastaanottajien tunnuksia
     * @param pagingAndSorting
     *            Sivutus- ja lajittelutiedot
     * @return Lista vastaanottajien osoitetietoja
     */
    List<LetterReceiverEmail> getLetterReceiverEmailByLetterReceiverID(List<Long> letterReceiverIDs, PagingAndSortingDTO pagingAndSorting);

}
