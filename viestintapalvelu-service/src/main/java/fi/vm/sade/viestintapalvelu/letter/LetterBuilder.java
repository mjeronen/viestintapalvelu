package fi.vm.sade.viestintapalvelu.letter;

import com.lowagie.text.DocumentException;
import fi.vm.sade.viestintapalvelu.Constants;
import fi.vm.sade.viestintapalvelu.address.AddressLabel;
import fi.vm.sade.viestintapalvelu.address.HtmlAddressLabelDecorator;
import fi.vm.sade.viestintapalvelu.address.XmlAddressLabelDecorator;
import fi.vm.sade.viestintapalvelu.document.DocumentBuilder;
import fi.vm.sade.viestintapalvelu.document.DocumentMetadata;
import fi.vm.sade.viestintapalvelu.document.MergedPdfDocument;
import fi.vm.sade.viestintapalvelu.document.PdfDocument;
import fi.vm.sade.viestintapalvelu.email.EmailSourceData;
import fi.vm.sade.viestintapalvelu.externalinterface.component.EmailComponent;
import fi.vm.sade.viestintapalvelu.template.Replacement;
import fi.vm.sade.viestintapalvelu.template.Template;
import fi.vm.sade.viestintapalvelu.template.TemplateContent;
import fi.vm.sade.viestintapalvelu.template.TemplateService;
import fi.vm.sade.viestintapalvelu.validator.LetterBatchValidator;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Singleton
public class LetterBuilder {

    private final Logger LOG = LoggerFactory.getLogger(LetterBuilder.class);

    private DocumentBuilder documentBuilder;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private LetterService letterService;

    @Autowired
    private EmailComponent emailComponent;

    @Inject
    public LetterBuilder(DocumentBuilder documentBuilder) {
        this.documentBuilder = documentBuilder;
    }

    public byte[] printZIP(LetterBatch batch) throws IOException, DocumentException, Exception {
        boolean valid = LetterBatchValidator.validate(batch);
        LOG.debug("Validated batch result: " + valid);

        Map<String, byte[]> subZips = new HashMap<String, byte[]>();
        List<LetterBatch> subBatches = batch.split(Constants.IPOST_BATCH_LIMIT);
        for (int i = 0; i < subBatches.size(); i++) {
            LetterBatch subBatch = subBatches.get(i);
            MergedPdfDocument pdf = buildPDF(subBatch);
            batch.setTemplateId(subBatch.getTemplateId()); // buildPDF fetches
                                                           // template id

            Map<String, Object> context = createIPostDataContext(pdf.getDocumentMetadata());
            String templateName = batch.getTemplateName();
            context.put("filename", templateName + ".pdf");
            byte[] ipostXml = documentBuilder.applyTextTemplate(Constants.LETTER_IPOST_TEMPLATE, context);

            Map<String, byte[]> documents = new HashMap<String, byte[]>();
            documents.put(templateName + ".pdf", pdf.toByteArray());
            documents.put(templateName + ".xml", ipostXml);
            String zipName = templateName + "_" + batch.getLanguageCode() + "_" + (i + 1) + ".zip";
            byte[] zip = documentBuilder.zip(documents);
            subZips.put(zipName, zip);
            batch.addIPostiData(zipName, zip);
        }
        byte[] resultZip = documentBuilder.zip(subZips);
        letterService.createLetter(batch);
        return resultZip;
    }

    public byte[] printPDF(LetterBatch batch) throws IOException, DocumentException, Exception {

        boolean valid = LetterBatchValidator.validate(batch);
        LOG.debug("Validated batch result: " + valid);

        MergedPdfDocument resultPDF = buildPDF(batch);
        // store batch to database
        letterService.createLetter(batch);
        return resultPDF.toByteArray();
    }

    private MergedPdfDocument buildPDF(LetterBatch batch) throws IOException, DocumentException {

        Template baseTemplate = getBaseTemplate(batch);
        Map<String, Object> baseReplacements = getTemplateReplacements(baseTemplate);

        List<PdfDocument> source = new ArrayList<PdfDocument>();

        // For updating letters content with the generated PdfDocument
        List<Letter> updatedLetters = new LinkedList<Letter>();

        for (Letter letter : batch.getLetters()) {

            // Use the base template and base replacements by default
            Template letterTemplate = baseTemplate;
            Map<String, Object> letterReplacements = baseReplacements;

            // Letter language != template language
            if (languageIsDifferent(baseTemplate, letter)) {
                // Get the template in user specific language
                Template template = templateService.getTemplateByName(letterTemplate.getName(), letter.getLanguageCode());
                if (template != null) {
                    letterTemplate = template;
                    letterReplacements = getTemplateReplacements(letterTemplate);
                }
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> dataContext = createDataContext(baseTemplate, letter.getAddressLabel(),
                    letterReplacements,  // Template replacements defaults from template
                    batch.getTemplateReplacements(),  // LetterBatch replacements common for all recipients
                    letter.getTemplateReplacements()); // Letter recipient level replacements
            
            EmailSourceData emailSource = null;
            if (shouldReceiveEmail(letter)) {
                try {
                    emailSource = new EmailSourceData(letter, letterTemplate, dataContext);
                } catch (Exception e) {
                   LOG.info("Could not handle email sending for {} reason {}", letter, e);
                   e.printStackTrace();
                }
            }
            
            if (letterTemplate != null) {
                List<TemplateContent> contents = letterTemplate.getContents();
                PdfDocument currentDocument = new PdfDocument(letter.getAddressLabel());
                Collections.sort(contents);
                for (TemplateContent tc : contents) {
                    byte[] page = createPagePdf(letterTemplate, tc.getContent().getBytes(), dataContext);
                    if (emailSource != null && tc.getName() != null && tc.getName().equalsIgnoreCase("liite")) {
                        // Hardcoded template content name "liite" for emailed attachments.. works for current templates
                        // in future templates should have "add as email attachment" -type property field which 
                        // would be checked here. 
                        emailSource.addAttachment("liite", page, "application/pdf");
                    }
                    currentDocument.addContent(page);
                }
                source.add(currentDocument);
                letter.setLetterContent(new LetterContent(documentBuilder.merge(currentDocument).toByteArray(), "application/pdf", new Date()));
            }
            
            sendEmail(emailSource);
            updatedLetters.add(letter);
        }

        // Write LetterBatch to DB
        batch.setLetters(updatedLetters); // Contains now the generated PdfDocuments
        // letterService.createLetter(batch);

        return documentBuilder.merge(source);
    }

    private boolean languageIsDifferent(Template baseTemplate, Letter letter) {
        return letter.getLanguageCode() != null && !letter.getLanguageCode().equalsIgnoreCase(baseTemplate.getLanguage());
    }

    private Template getBaseTemplate(LetterBatch batch) throws IOException {
        // Get the given value
        Template template = batch.getTemplate();

        // Search template by name
        if (template == null && batch.getTemplateName() != null && batch.getLanguageCode() != null) {
            template = templateService.getTemplateByName(batch.getTemplateName(), batch.getLanguageCode());
            batch.setTemplateId(template.getId()); // update template Id
        }

        // Search template by id
        if (template == null && batch.getTemplateId() != null) {
            template = templateService.findById(batch.getTemplateId());
        }

        // Fail, if template is still not found
        if (template == null) {
            throw new IOException("Could not locate template resource.");
        }
        return template;
    }

    private Map<String, Object> getTemplateReplacements(Template template) {
        Map<String, Object> replacements = new HashMap<String, Object>();
        for (Replacement r : template.getReplacements()) {
            replacements.put(r.getName(), r.getDefaultValue());
        }
        return replacements;
    }

    /**
     * Create content
     * 
     * @param templateName
     * @param languageCode
     * @param type
     * @return email content
     */
    public String getTemplateContent(String templateName, String languageCode, String type) throws IOException, DocumentException {

        // Get the template
        Template template = templateService.getTemplateByName(templateName, languageCode, type);
        if (template == null)
            throw new IOException("could not locate template resource.");

        // Get template replacements
        Map<String, Object> templateReplacements = getTemplateReplacements(template);

        List<byte[]> source = new ArrayList<byte[]>();

        if (template != null) {
            List<TemplateContent> contents = template.getContents();

            Collections.sort(contents);

            // Generate each page individually
            for (TemplateContent tc : contents) {
                byte[] page = createPageXhtml(template, tc.getContent().getBytes(), templateReplacements);
                source.add(page);
            }
        }

        byte[] result = documentBuilder.mergeByte(source);

        return new String(result);
    }

    private byte[] createPagePdf(Template template, byte[] pageContent, Map<String, Object> dataContext) throws FileNotFoundException, IOException, DocumentException {

        @SuppressWarnings("unchecked")
        //Map<String, Object> dataContext = createDataContext(template, addressLabel, templReplacements, letterBatchReplacements, letterReplacements);
        byte[] xhtml = documentBuilder.applyTextTemplate(pageContent, dataContext);
        return documentBuilder.xhtmlToPDF(xhtml);
    }

    
    /**
     * Create page as XHTML
     * 
     * @param template
     * @param pageContent
     * @param templateReplacements
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     * @throws DocumentException
     */
    private byte[] createPageXhtml(Template template, byte[] pageContent, Map<String, Object> templateReplacements) throws FileNotFoundException, IOException,
            DocumentException {

        @SuppressWarnings("unchecked")
        Map<String, Object> dataContext = createDataContext(template, templateReplacements);
        return documentBuilder.applyTextTemplate(pageContent, dataContext);
    }

    
    private Map<String, Object> createDataContext(Template template, AddressLabel addressLabel, Map<String, Object>... replacementsList) {

        Map<String, Object> data = new HashMap<String, Object>();
        for (Map<String, Object> replacements : replacementsList) {
            if (replacements != null) {
                for (String key : replacements.keySet()) {
                    if (replacements.get(key) instanceof String) {
                        data.put(key, cleanHtmlFromApi((String) replacements.get(key)));
                    } else {
                        data.put(key, replacements.get(key));
                    }
                }
            }
        }

        String styles = template.getStyles();
        if (styles == null) {
            styles = "";
        }
        data.put("letterDate", new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
        data.put("osoite", new HtmlAddressLabelDecorator(addressLabel));
        data.put("addressLabel", new XmlAddressLabelDecorator(addressLabel));

        // liite specific handling
        if (data.containsKey("tulokset")) {
            List<Map<String, String>> tulokset = (List<Map<String, String>>) data.get("tulokset");
            Map<String, Boolean> columns = distinctColumns(tulokset);
            data.put("tulokset", normalizeColumns(columns, tulokset));
            data.put("columns", columns);
        }
        if (data.containsKey("muut_hakukohteet")) {
            List<String> muidenHakukohteidenNimet = (List<String>) data.get("muut_hakukohteet");
            data.put("muut_hakukohteet", muidenHakukohteidenNimet);
        }

        data.put("tyylit", styles);
        return data;
    }

    /**
     * Create data context
     * 
     * @param template
     * @param replacementsList
     * @return
     */
    private Map<String, Object> createDataContext(Template template, Map<String, Object>... replacementsList) {

        Map<String, Object> data = new HashMap<String, Object>();
        for (Map<String, Object> replacements : replacementsList) {
            if (replacements != null) {
                for (String key : replacements.keySet()) {
                    if (replacements.get(key) instanceof String) {
                        data.put(key, cleanHtmlFromApi((String) replacements.get(key)));
                    } else {
                        data.put(key, replacements.get(key));
                    }
                }
            }
        }

        String styles = template.getStyles();
        if (styles == null) {
            styles = "";
        }
        data.put("tyylit", styles);
        return data;
    }

    private List<Map<String, String>> normalizeColumns(Map<String, Boolean> columns, List<Map<String, String>> tulokset) {
        for (Map<String, String> row : tulokset) {
            for (String column : columns.keySet()) {
                if (!row.containsKey(column) || row.get(column) == null) {
                    row.put(column, "");
                }
                row.put(column, cleanHtmlFromApi(row.get(column)));
            }
        }
        return tulokset;
    }

    private Map<String, Boolean> distinctColumns(List<Map<String, String>> tulokset) {
        Map<String, Boolean> printedColumns = new HashMap<String, Boolean>();
        for (Map<String, String> haku : tulokset) {
            for (String column : haku.keySet()) {
                printedColumns.put(column, true);
            }
        }
        return printedColumns;
    }

    private Map<String, Object> createIPostDataContext(final List<DocumentMetadata> documentMetadataList) {
        Map<String, Object> data = new HashMap<String, Object>();
        List<Map<String, Object>> metadataList = new ArrayList<Map<String, Object>>();
        for (DocumentMetadata documentMetadata : documentMetadataList) {
            Map<String, Object> metadata = new HashMap<String, Object>();
            metadata.put("startPage", documentMetadata.getStartPage());
            metadata.put("pages", documentMetadata.getPages());
            metadata.put("addressLabel", new XmlAddressLabelDecorator(documentMetadata.getAddressLabel()));
            metadataList.add(metadata);
        }
        data.put("metadataList", metadataList);
        data.put("ipostTest", Constants.IPOST_TEST);
        return data;
    }

    private String cleanHtmlFromApi(String string) {
        return Jsoup.clean(string, Whitelist.relaxed());
    }

    private boolean shouldReceiveEmail(Letter letter) {
        return (letter.getEmailAddress() != null && !letter.getEmailAddress().isEmpty());
    }
    
    private void sendEmail(EmailSourceData source) throws IOException {
        if (source != null) {
            emailComponent.sendEmail(source);
        }
    }
}
