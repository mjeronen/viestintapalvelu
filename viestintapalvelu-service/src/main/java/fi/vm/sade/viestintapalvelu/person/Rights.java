/**
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
package fi.vm.sade.viestintapalvelu.person;

import java.io.Serializable;
import java.util.List;

/**
 * @author risal1
 *
 */
public class Rights implements Serializable {

    private static final long serialVersionUID = 5752349125449442146L;
    
    public final List<String> organizationOids;
    
    public final boolean ophUser;
    
    public final boolean rightToViewTemplates;
    
    public final boolean rightToEditTemplates;
    
    public final boolean rightToEditDrafts;

    public Rights(List<String> organizationOids, boolean ophUser, boolean rightToViewTemplates, boolean rightToEditTemplates, boolean rightToEditDrafts) {
        this.organizationOids = organizationOids;
        this.ophUser = ophUser;
        this.rightToViewTemplates = rightToViewTemplates;
        this.rightToEditTemplates = rightToEditTemplates;
        this.rightToEditDrafts = rightToEditDrafts;
    }
    
}
