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
package fi.vm.sade.viestintapalvelu.validator;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import fi.vm.sade.viestintapalvelu.api.address.AddressLabel;
import fi.vm.sade.viestintapalvelu.letter.Letter;
import fi.vm.sade.viestintapalvelu.letter.LetterBatch;
import fi.vm.sade.viestintapalvelu.letter.dto.LetterBatchDetails;
import fi.vm.sade.viestintapalvelu.testdata.DocumentProviderTestData;

public class LetterBatchValidatorTest {

    @Test(expected = IllegalArgumentException.class)
    public void throwExceptionIfGivenNullLetterBatch() throws Exception {
        LetterBatchValidator.validate((LetterBatchDetails) null);
    }

    @Test()
    public void FirstNameIsNull() throws Exception {
        Map<String, String> map = LetterBatchValidator.validate(givenBatchWithLetters(givenLetterWithAddressLabelWithNullField("firstName")));
        assertTrue(map != null && map.size() == 1);
    }

    public void containsErrorMessagesIfSurnameIsNull() throws Exception {
        Map<String, String> map = LetterBatchValidator.validate(givenBatchWithLetters(givenLetterWithAddressLabelWithNullField("lastName")));
        assertTrue(map != null && map.size() == 1);
    }

    public void containsErrorMessagesIfStreetAddressIsNull() throws Exception {
        Map<String, String> map = LetterBatchValidator.validate(givenBatchWithLetters(givenLetterWithAddressLabelWithNullField("addressline")));
        assertTrue(map != null && map.size() == 1);

    }

    public void containsErrorMessagesIfPostOfficeIsNull() throws Exception {
        Map<String, String> map = LetterBatchValidator.validate(givenBatchWithLetters(givenLetterWithAddressLabelWithNullField("city")));
        assertTrue(map != null && map.size() == 1);
    }

    public void containsErrorMessagesIfPostalCodeIsNull() throws Exception {
        Map<String, String> map = LetterBatchValidator.validate(givenBatchWithLetters(givenLetterWithAddressLabelWithNullField("postalCode")));
        assertTrue(map != null && map.size() == 1);
    }

    @Test
    public void returnsFalseForInvalidLetterBatch() {
        assertFalse(LetterBatchValidator.isValid(null));
    }

    @Test
    public void returnsTrueForValidLetterBatch() {
        assertTrue(LetterBatchValidator.isValid(DocumentProviderTestData.getAsyncLetterBatch()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void throwsExceptionForMissingTemplateName() throws Exception {
        LetterBatchValidator.validate(givenBatchWithLettersAndWithoutTemplate());
    }

    private AddressLabel givenAddressLabelWithNullField(String fieldName) throws Exception {
        AddressLabel label = DocumentProviderTestData.getAddressLabel();
        Field field = label.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(label, null);
        return label;
    }

    private Letter givenLetterWithAddressLabelWithNullField(String fieldName) throws Exception {
        Letter letter = new Letter();
        letter.setAddressLabel(givenAddressLabelWithNullField(fieldName));
        return letter;
    }

    private LetterBatch givenBatchWithLetters(Letter... letters) {
        LetterBatch batch = givenBatchWithLettersAndWithoutTemplate(letters);
        batch.setTemplateName("template");
        batch.setLanguageCode("358");
        return batch;
    }

    private LetterBatch givenBatchWithLettersAndWithoutTemplate(Letter... letters) {
        LetterBatch batch = new LetterBatch();
        batch.setLetters(Arrays.asList(letters));
        return batch;
    }

}
