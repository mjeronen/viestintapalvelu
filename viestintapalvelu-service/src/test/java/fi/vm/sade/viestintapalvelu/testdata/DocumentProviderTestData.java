package fi.vm.sade.viestintapalvelu.testdata;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fi.vm.sade.authentication.model.Henkilo;
import fi.vm.sade.viestintapalvelu.address.AddressLabel;
import fi.vm.sade.viestintapalvelu.letter.Letter;
import fi.vm.sade.viestintapalvelu.letter.LetterBatch;
import fi.vm.sade.viestintapalvelu.letter.LetterContent;
import fi.vm.sade.viestintapalvelu.model.LetterReceiverAddress;
import fi.vm.sade.viestintapalvelu.model.LetterReceiverLetter;
import fi.vm.sade.viestintapalvelu.model.LetterReceiverReplacement;
import fi.vm.sade.viestintapalvelu.model.LetterReceivers;
import fi.vm.sade.viestintapalvelu.model.LetterReplacement;
import fi.vm.sade.viestintapalvelu.model.Replacement;
import fi.vm.sade.viestintapalvelu.model.Template;
import fi.vm.sade.viestintapalvelu.model.TemplateContent;

public class DocumentProviderTestData {
    public static AddressLabel getAddressLabel() {
        AddressLabel addressLabel = new AddressLabel("Etunimi", "Sukunimi", "Testiosoite 1", "Testiosoite 2", 
            "Testiosoite 3", "00100", "Helsinki", "", "Suomi", "FI");
        
        return addressLabel;
    }
    
    public static Henkilo getHenkilo() {
        Henkilo henkilo = new Henkilo();
        
        henkilo.setOidHenkilo("1.2.246.562.24.34397748041");
        henkilo.setHetu("081181-9984");
        henkilo.setEtunimet("Etunimi");
        henkilo.setSukunimi("Sukunimi");
        henkilo.setKutsumanimi("Kutsumanimi");
        
        return henkilo;
    }

    public static fi.vm.sade.viestintapalvelu.letter.LetterBatch getLetterBatch() {
        LetterBatch letterBatch = new LetterBatch();
        
        letterBatch.setApplicationPeriod("Test-2014");
        letterBatch.setFetchTarget("test-fetchTarget");
        letterBatch.setLanguageCode("FI");
        letterBatch.setOrganizationOid("1.2.246.562.10.00000000001");
        letterBatch.setStoringOid("1.2.246.562.24.00000000001");
        letterBatch.setTag("test-tag");
        letterBatch.setTemplateId(new Long(1));
        letterBatch.setTemplateName("test-templateName");
        letterBatch.setTemplate(getTemplate());
        letterBatch.setTemplateReplacements(getTemplateReplacements());
        letterBatch.setLetters(getLetters());
        
        return letterBatch;
    }

    public static fi.vm.sade.viestintapalvelu.model.LetterBatch getLetterBatch(Long id) {
        fi.vm.sade.viestintapalvelu.model.LetterBatch letterBatch = new fi.vm.sade.viestintapalvelu.model.LetterBatch();
        
        letterBatch.setApplicationPeriod("Tradenomi 2014");
        letterBatch.setFetchTarget("fetchTarget");
        
        if (id != null) {
            letterBatch.setId(id);
        }
        
        letterBatch.setLanguage("FI");
        letterBatch.setOrganizationOid("1.2.246.562.10.00000000001");
        letterBatch.setStoringOid("1.2.246.562.24.00000000001");
        letterBatch.setTag("test-tag");
        letterBatch.setTemplateId(new Long(1));
        letterBatch.setTemplateName("test-templateName");
        letterBatch.setTimestamp(new Date());
        letterBatch.setVersion(new Long(0));
        
        letterBatch.setLetterReceivers(getLetterReceivers(id, letterBatch));
        letterBatch.setLetterReplacements(getLetterReplacements(id, letterBatch));
        
        return letterBatch;
    }
    
    public static LetterContent getLetterContent() {
        LetterContent letterContent = new LetterContent();
        
        byte[] testContent = {'t','e','s','t','c','o','n','t','e','n','t'};
        letterContent.setContent(testContent);
        letterContent.setContentType("application/msword");
        letterContent.setTimestamp(new Date());
           
        return letterContent;
    }   
    
    public static LetterReceiverAddress getLetterReceiverAddress(Long id, LetterReceivers letterReceivers) {
        LetterReceiverAddress letterReceiverAddress = new LetterReceiverAddress();
        
        letterReceiverAddress.setAddressline("Testiosoite 1");
        letterReceiverAddress.setAddressline2("Testiosoite 2");
        letterReceiverAddress.setAddressline3("Testiosoite 3");
        letterReceiverAddress.setCity("Helsinki");
        letterReceiverAddress.setCountry("Suomi");
        letterReceiverAddress.setCountryCode("FI");
        letterReceiverAddress.setFirstName("Etunimi");
        
        if (id != null) {
            letterReceiverAddress.setId(id);
        }
        
        letterReceiverAddress.setLastName("Sukunimi");
        letterReceiverAddress.setLetterReceivers(letterReceivers);
        letterReceiverAddress.setPostalCode("00100");
        letterReceiverAddress.setRegion("");
        letterReceiverAddress.setVersion(new Long(1));
        
        return letterReceiverAddress;
    }

    public static LetterReceiverLetter getLetterReceiverLetter(Long id, LetterReceivers letterReceivers) {
        LetterReceiverLetter letterReceiverLetter = new LetterReceiverLetter();
        
        byte[] letter = {'l','e','t','t','e','r'}; 
        
        letterReceiverLetter.setContentType("application/msword");
        
        if (id != null) {
            letterReceiverLetter.setId(id);
        }
        
        letterReceiverLetter.setLetter(letter);
        letterReceiverLetter.setLetterReceivers(letterReceivers);
        letterReceiverLetter.setOriginalContentType("application/msword");
        letterReceiverLetter.setTimestamp(new Date());
        letterReceiverLetter.setVersion(new Long(0));
        
        return letterReceiverLetter;
    }

    public static Set<LetterReceiverReplacement> getLetterReceiverReplacement(Long id, LetterReceivers letterReceivers) {
        Set<LetterReceiverReplacement> letterReceiverReplacements = new HashSet<LetterReceiverReplacement>();
        
        LetterReceiverReplacement letterReceiverReplacement = new LetterReceiverReplacement();
        letterReceiverReplacement.setDefaultValue("");
        
        if (id != null) {
            letterReceiverReplacement.setId(id);
        }
        
        letterReceiverReplacement.setLetterReceivers(letterReceivers);
        letterReceiverReplacement.setMandatory(false);
        letterReceiverReplacement.setName("Nimi");
        letterReceiverReplacement.setTimestamp(new Date());
        letterReceiverReplacement.setVersion(new Long(0));
        
        letterReceiverReplacements.add(letterReceiverReplacement);
        
        return letterReceiverReplacements;
    }

    public static Set<LetterReceivers> getLetterReceivers(Long id, 
        fi.vm.sade.viestintapalvelu.model.LetterBatch letterBatch) {
        Set<LetterReceivers> letterReceiversSet = new HashSet<LetterReceivers>();
        
        LetterReceivers letterReceivers = new LetterReceivers();
        
        if (id != null) {
            letterReceivers.setId(id);
        }
        
        letterReceivers.setLetterBatch(letterBatch);
        letterReceivers.setTimestamp(new Date());
        letterReceivers.setVersion(new Long(0));
        letterReceivers.setLetterReceiverAddress(getLetterReceiverAddress(id, letterReceivers));
        letterReceivers.setLetterReceiverLetter(getLetterReceiverLetter(id, letterReceivers));
        letterReceivers.setLetterReceiverReplacement(getLetterReceiverReplacement(id ,letterReceivers));
        
        letterReceiversSet.add(letterReceivers);
        
        return letterReceiversSet;
    }

    public static Set<LetterReplacement> getLetterReplacements(Long id, 
        fi.vm.sade.viestintapalvelu.model.LetterBatch letterBatch) {
        Set<LetterReplacement> letterReplacements = new HashSet<LetterReplacement>();
        
        LetterReplacement letterReplacement = new LetterReplacement();
        letterReplacement.setDefaultValue("");
        
        if (id != null) {
            letterReplacement.setId(id);
        }
        
        letterReplacement.setLetterBatch(letterBatch);
        letterReplacement.setMandatory(false);
        letterReplacement.setName("test-replacement-name");
        letterReplacement.setTimestamp(new Date());
        letterReplacement.setVersion(new Long(0));
        
        letterReplacements.add(letterReplacement);
        
        return letterReplacements;
    }

    public static List<Letter> getLetters() {
        List<Letter> letters = new ArrayList<Letter>();
        
        Letter letter = new Letter();
        letter.setAddressLabel(getAddressLabel());
        letter.setLanguageCode("FI");
        letter.setLetterContent(getLetterContent());
        letter.setTemplateReplacements(getTemplateReplacements());
        
        letters.add(letter);
        
        return letters;
    }

    public static Replacement getReplacement(Long id, Template template) {
        Replacement replacement = new Replacement();
        
        if (id != null) {
            replacement.setId(id);
        }
        
        replacement.setDefaultValue("test");
        replacement.setMandatory(false);
        replacement.setName("test_replacement");
        replacement.setTemplate(template);
        replacement.setTimestamp(new Date());
        
        return replacement;
    }

    public static List<fi.vm.sade.viestintapalvelu.template.Replacement> getReplacements() {
        List<fi.vm.sade.viestintapalvelu.template.Replacement> replacements = 
            new ArrayList<fi.vm.sade.viestintapalvelu.template.Replacement>();
        
        fi.vm.sade.viestintapalvelu.template.Replacement replacement = 
            new fi.vm.sade.viestintapalvelu.template.Replacement();
        replacement.setDefaultValue("");
        replacement.setMandatory(false);
        replacement.setName("test-name");
        replacement.setTimestamp(new Date());
        
        replacements.add(replacement);
        
        return replacements;
    }

    public static fi.vm.sade.viestintapalvelu.template.Template getTemplate() {
        fi.vm.sade.viestintapalvelu.template.Template template = new fi.vm.sade.viestintapalvelu.template.Template();
        
        template.setLanguage("FI");
        template.setName("test_template");
        template.setOrganizationOid("1.2.246.562.10.00000000001");
        template.setStoringOid("1.2.246.562.24.00000000001");
        template.setStyles("test-styles");
        template.setTimestamp(new Date());
        template.setContents(getTemplateContents());
        template.setReplacements(getReplacements());
    
        return template;
    }

    public static Template getTemplate(Long id) {
        Template template = new Template();
        
        if (id != null) {
            template.setId(id);
        }
        
        template.setLanguage("FI");
        template.setName("test_template");
        template.setOrganizationOid("1.2.246.562.10.00000000001");
        template.setStoringOid("1.2.246.562.24.00000000001");
        template.setStyles("test-styles");
        template.setTimestamp(new Date());
        template.setVersionro("1.0");
        
        Set<Replacement> replacements = new HashSet<Replacement>();
        replacements.add(getReplacement(id, template));
        
        Set<TemplateContent> templateContents = new HashSet<TemplateContent>();
        templateContents.add(getTemplateContent(id, template));
        
        template.setContents(templateContents);
        template.setReplacements(replacements);
        
        return template;
    }

    public static TemplateContent getTemplateContent(Long id, Template template) {
        TemplateContent templateContent = new TemplateContent();
        
        if (id != null) {
            templateContent.setId(id);
        }
        
        templateContent.setContent("test_content");
        templateContent.setContentType("pdf");
        templateContent.setName("test_template_content");
        templateContent.setOrder(1);
        templateContent.setTemplate(template);
        templateContent.setTimestamp(new Date());
        
        return templateContent;
    }

    public static List<fi.vm.sade.viestintapalvelu.template.TemplateContent> getTemplateContents() {
        List<fi.vm.sade.viestintapalvelu.template.TemplateContent> templateContents = 
            new ArrayList<fi.vm.sade.viestintapalvelu.template.TemplateContent>();
        
        fi.vm.sade.viestintapalvelu.template.TemplateContent templateContent = 
            new fi.vm.sade.viestintapalvelu.template.TemplateContent();
        templateContent.setContent("test_content");
        templateContent.setName("test_template_content");
        templateContent.setOrder(1);
        templateContent.setTimestamp(new Date());
        
        templateContents.add(templateContent);
        
        return templateContents;
    }

    public static Map<String, Object> getTemplateReplacements() {
        Map<String, Object> templateReplacements = new HashMap<String, Object>();
        templateReplacements.put("key-1", new String("templatereplacemet-1"));
        templateReplacements.put("key-2", new String("templatereplacemet-2"));
        
        return templateReplacements;
    }
 }