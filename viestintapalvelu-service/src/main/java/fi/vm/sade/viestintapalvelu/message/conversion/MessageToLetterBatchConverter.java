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
package fi.vm.sade.viestintapalvelu.message.conversion;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import fi.vm.sade.viestintapalvelu.api.message.MessageData;
import fi.vm.sade.viestintapalvelu.api.message.Receiver;
import fi.vm.sade.viestintapalvelu.letter.dto.AsyncLetterBatchDto;
import fi.vm.sade.viestintapalvelu.letter.dto.AsyncLetterBatchLetterDto;

/**
 * @author risal1
 *
 */
public class MessageToLetterBatchConverter implements MessageDataConverter<MessageData, AsyncLetterBatchDto> {

    @Override
    public ConvertedMessageWrapper<AsyncLetterBatchDto> convert(MessageData data) {
        AsyncLetterBatchDto batch = new AsyncLetterBatchDto();
        batch.setTemplateName(data.templateName);
        batch.setLanguageCode(data.language);
        batch.setTemplateReplacements(data.commonReplacements);
        batch.setLetters(convertMessages(data.receivers));
        // TODO other optional fields?
        return new ConvertedMessageWrapper<AsyncLetterBatchDto>(batch, new ArrayList<Receiver>());
    }
    
    private List<AsyncLetterBatchLetterDto> convertMessages(List<Receiver> receivers) {
        return Lists.transform(receivers, new Function<Receiver, AsyncLetterBatchLetterDto>() {

            @Override
            public AsyncLetterBatchLetterDto apply(Receiver input) {
                AsyncLetterBatchLetterDto dto = new AsyncLetterBatchLetterDto();
                dto.setAddressLabel(input.addressLabel);
                dto.setTemplateReplacements(input.replacements);
                dto.setLanguageCode(input.language);
                return dto;
            }
            
        });
    }

}
