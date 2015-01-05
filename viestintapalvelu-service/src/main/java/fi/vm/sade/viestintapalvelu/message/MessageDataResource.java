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
package fi.vm.sade.viestintapalvelu.message;

import fi.vm.sade.viestintapalvelu.api.message.MessageData;
import fi.vm.sade.viestintapalvelu.api.message.MessageStatusResponse;
import fi.vm.sade.viestintapalvelu.api.rest.MessageResource;

/**
 * @author risal1
 *
 */
public class MessageDataResource implements MessageResource {

    /* (non-Javadoc)
     * @see fi.vm.sade.viestintapalvelu.api.rest.MessageResource#sendMessageViaAsiointiTiliOrEmail(fi.vm.sade.viestintapalvelu.api.message.MessageData)
     */
    @Override
    public MessageStatusResponse sendMessageViaAsiointiTiliOrEmail(MessageData messageData) {
        // TODO Auto-generated method stub
        return null;
    }

}
