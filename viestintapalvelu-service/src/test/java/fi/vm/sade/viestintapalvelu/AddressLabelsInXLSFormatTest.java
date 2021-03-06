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
package fi.vm.sade.viestintapalvelu;

import org.junit.Ignore;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@Ignore
@RunWith(Enclosed.class)
public class AddressLabelsInXLSFormatTest {
    //public static TomcatRule tomcat = new TomcatRule();

   /* public static class WhenCreatingLabelForValidForeignAddress {

        private static AddressLabel label = new AddressLabel("Åle", "Öistämö",
                "Brännkyrksgatan 177 B 149", "Södermalm", "13", "65330",
                "Stockholm", "SL", "Sweden", "SE");
        private static List<String> otsikko;
        private static List<String> osoite;

        @BeforeClass
        public static void setUp() throws Exception {
            List<List<String>> responseBody = TestUtil
                    .generateAddressLabelsXLS(Arrays.asList(label));
            otsikko = responseBody.get(0);
            osoite = responseBody.get(1);
        }

        @Test
        public void firstNameIsMappedToFirstColumn() throws Exception {
            assertEquals("Firstname", otsikko.get(0));
            assertEquals(label.getFirstName(), osoite.get(0));
        }

        @Test
        public void lastNameIsMappedToSecondColumn() throws Exception {
            assertEquals("Lastname", otsikko.get(1));
            assertEquals(label.getLastName(), osoite.get(1));
        }

        @Test
        public void addresslineIsMappedToThirdColumn() throws Exception {
            assertEquals("Addressline", otsikko.get(2));
            assertEquals(label.getAddressline(), osoite.get(2));
        }

        @Test
        public void addressline2IsMappedToFouthColumn() throws Exception {
            assertEquals("Addressline2", otsikko.get(3));
            assertEquals(label.getAddressline2(), osoite.get(3));
        }

        @Test
        public void addressline3IsMappedToFifthColumn() throws Exception {
            assertEquals("Addressline3", otsikko.get(4));
            assertEquals(label.getAddressline3(), osoite.get(4));
        }

        @Test
        public void postalCodeIsMappedToSixthColumn() throws Exception {
            assertEquals("Postal Code", otsikko.get(5));
            assertEquals(label.getPostalCode(), osoite.get(5));
        }

        @Test
        public void cityIsMappedToSeventhColumn() throws Exception {
            assertEquals("City", otsikko.get(6));
            assertEquals(label.getCity(), osoite.get(6));
        }

        @Test
        public void regionIsMappedToEightColumn() throws Exception {
            assertEquals("Region", otsikko.get(7));
            assertEquals(label.getRegion(), osoite.get(7));
        }

        @Test
        public void countryIsMappedToNinethColumn() throws Exception {
            assertEquals("Country", otsikko.get(8));
            assertEquals(label.getCountry(), osoite.get(8));
        }

        @Test
        public void rowContainsNineColumns() throws Exception {
            assertEquals(9, osoite.size());
        }
    }

    public static class WhenCreatingLabelWithSpecialCharacters {
        @Test
        public void specialCharactersAreDisplayed() throws Exception {
            List<String> label = callGenerateLabels("Åle &", "Öistämö #",
                    "Brännkyrksgatan 177 B 149&", "Södermalm $€", "13@",
                    "65330&", "Stockholm&", "SL&", "Sweden&", "SE");
            assertEquals("Åle &", label.get(0));
            assertEquals("Öistämö #", label.get(1));
            assertEquals("Brännkyrksgatan 177 B 149&", label.get(2));
            assertEquals("Södermalm $€", label.get(3));
            assertEquals("13@", label.get(4));
            assertEquals("65330&", label.get(5));
            assertEquals("Stockholm&", label.get(6));
            assertEquals("SL&", label.get(7));
            assertEquals("Sweden&", label.get(8));
        }
    }

    public static class WhenFirstNameIsEmpty {
        @Test
        public void firstColumnIsEmpty() throws Exception {
            assertEquals(
                    "",
                    callGenerateLabels("", "Öistämö",
                            "Brännkyrksgatan 177 B 149", "Södermalm", "13",
                            "65330", "Stockholm", "SL", "Sweden", "SE").get(0));
        }
    }

    public static class WhenLastNameIsEmpty {
        @Test
        public void secondColumnIsEmpty() throws Exception {
            assertEquals(
                    "",
                    callGenerateLabels("Åle", "", "Brännkyrksgatan 177 B 149",
                            "Södermalm", "13", "65330", "Stockholm", "SL",
                            "Sweden", "SE").get(1));
        }
    }

    public static class WhenAddresslineIsEmpty {
        @Test
        public void thirdColumnIsEmpty() throws Exception {
            assertEquals(
                    "",
                    callGenerateLabels("Åle", "Öistämö", "", "Södermalm", "13",
                            "65330", "Stockholm", "SL", "Sweden", "SE").get(2));
        }
    }

    public static class WhenAddressline2IsEmpty {
        @Test
        public void fourthColumnIsEmpty() throws Exception {
            assertEquals(
                    "",
                    callGenerateLabels("Åle", "Öistämö",
                            "Brännkyrksgatan 177 B 149", "", "13", "65330",
                            "Stockholm", "SL", "Sweden", "SE").get(3));
        }
    }

    public static class WhenAddressline3IsEmpty {
        @Test
        public void fifthColumnIsEmpty() throws Exception {
            assertEquals(
                    "",
                    callGenerateLabels("Åle", "Öistämö",
                            "Brännkyrksgatan 177 B 149", "Södermalm", "",
                            "65330", "Stockholm", "SL", "Sweden", "SE").get(4));
        }
    }

    public static class WhenPostalCodeIsEmpty {
        @Test
        public void sixthColumnIsEmptyString() throws Exception {
            assertEquals(
                    "",
                    callGenerateLabels("Åle", "Öistämö",
                            "Brännkyrksgatan 177 B 149", "Södermalm", "13", "",
                            "Stockholm", "SL", "Sweden", "SE").get(5));
        }
    }

    public static class WhenPostOfficeIsEmpty {
        @Test
        public void seventhColumnIsEmptyString() throws Exception {
            assertEquals(
                    "",
                    callGenerateLabels("Åle", "Öistämö",
                            "Brännkyrksgatan 177 B 149", "Södermalm", "13",
                            "65330", "", "SL", "Sweden", "SE").get(6));
        }
    }

    public static class WhenRegionIsEmpty {
        @Test
        public void eightColumnIsEmptyString() throws Exception {
            assertEquals(
                    "",
                    callGenerateLabels("Åle", "Öistämö",
                            "Brännkyrksgatan 177 B 149", "Södermalm", "13",
                            "65330", "Stockholm", "", "Sweden", "SE").get(7));
        }
    }

    public static class WhenCountryIsEmpty {
        @Test
        public void ninethColumnIsEmptyString() throws Exception {
            assertEquals(
                    "",
                    callGenerateLabels("Åle", "Öistämö",
                            "Brännkyrksgatan 177 B 149", "Södermalm", "13",
                            "65330", "Stockholm", "SL", "", "FI").get(8));
        }
    }

    public static class WhenAddressIsLocal {
        @Test
        public void seventhColumnIsEmptyString() throws Exception {
            List<String> label = callGenerateLabels("Åle", "Öistämö",
                    "Mannerheimintie 177 B 149", "", "", "65330", "Helsinki",
                    "", "Finland", "FI");
            assertEquals("", label.get(8));
        }
    }

    public static class WhenAddressIsLocalAndCountryIsUppercaseFINLAND {
        @Test
        public void seventhColumnIsEmptyString() throws Exception {
            assertEquals(
                    "",
                    callGenerateLabels("Åle", "Öistämö",
                            "Mannerheimintie 177 B 149", "", "", "65330",
                            "Helsinki", "", "FINLAND", "FI").get(8));
        }
    }

    public static class WhenCreatingLabelsInABigBatch {

        private static List<AddressLabel> batch;
        private static List<List<String>> responseBody;

        @BeforeClass
        public static void setUp() throws Exception {
            batch = createLabels(1000);
            responseBody = TestUtil.generateAddressLabelsXLS(batch);
        }

        @Test
        public void returnedXLSContainsHeaderAndEqualAmountOfLabels()
                throws Exception {
            assertEquals(batch.size() + 1, responseBody.size());
        }
    }

    private static List<AddressLabel> createLabels(int count)
            throws JsonParseException, JsonMappingException, IOException {
        return new Generator<AddressLabel>() {
            protected AddressLabel createObject(TestData testData) {
                String postOffice = testData.random("postOffice");
                String[] country = testData.randomArray("country");
                return new AddressLabel(testData.random("firstname"),
                        testData.random("lastname"), testData.random("street")
                        + " " + testData.random("houseNumber"), "", "",
                        postOffice.substring(0, postOffice.indexOf(" ")),
                        postOffice.substring(postOffice.indexOf(" ") + 1), "",
                        country[0], country[1]);
            }
        }.generateObjects(count);
    }

    private static List<String> callGenerateLabels(String firstName,
                                                   String lastName, String addressline, String addressline2,
                                                   String addressline3, String postalCode, String city, String region,
                                                   String country, String countryCode) throws Exception {
        AddressLabel label = new AddressLabel(firstName, lastName, addressline,
                addressline2, addressline3, postalCode, city, region, country,
                countryCode);
        return TestUtil.generateAddressLabelsXLS(Arrays.asList(label)).get(1);
    }*/
}
