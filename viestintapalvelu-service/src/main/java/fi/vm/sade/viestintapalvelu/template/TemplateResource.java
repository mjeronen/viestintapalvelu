package fi.vm.sade.viestintapalvelu.template;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.lowagie.text.DocumentException;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiImplicitParam;
import com.wordnik.swagger.annotations.ApiImplicitParams;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import fi.vm.sade.viestintapalvelu.AsynchronousResource;
import fi.vm.sade.viestintapalvelu.Urls;
import fi.vm.sade.viestintapalvelu.Utils;
import fi.vm.sade.viestintapalvelu.letter.LetterService;
import fi.vm.sade.viestintapalvelu.validator.UserRightsValidator;

@Component
@PreAuthorize("isAuthenticated()")
@Path(Urls.TEMPLATE_RESOURCE_PATH)
@Api(value = "/" + Urls.API_PATH + "/" + Urls.TEMPLATE_RESOURCE_PATH, description = "Kirjepohjarajapinta")
public class TemplateResource extends AsynchronousResource {  
    @Autowired 
    private TemplateService templateService;   
    @Autowired 
    private LetterService letterService;
    @Autowired
    private UserRightsValidator userRightsValidator;
        
    private final static String GetHistory = "Palauttaa kirjepohjan historian";
    private final static String GetHistory2 = "Palauttaa listan MAPeja. Ainakin yksi, tällä hetkellä jopa kolme.<br>"
    		+ "Kukin sisältää MAPin nimen (name) ja listan korvauskenttiä (templateReplacements) <br>"
    		+ " - default: pohjan korvauskentät <br>"
    		+ " - organizationLatest: organisaatiokohtaiset viimeiset pohjan korvauskentät<br> "
    		+ " - organizationLatestByTag: edelliseen tarkennettuna tunnisteeella ";
    private final static String GetHistory200 = "Hakijalla ei ole valtuuksia hakea kirjepohjia.";
    
    private final static String TemplateNames = "Palauttaa valittavissaolevien kirjepohjien nimet.";    
    private final static String TemplateNames2 = "Palauttaa listan MAPeja. Esim: <br>"
    												+ "{ <br>"
													+ "'name': 'jalkiohjauskirje', <br>"
													+ "'lang': 'FI' <br>"
													+ "}";
    
    
    private final static String ApitemplateByName = "Palauttaa kirjepohjan nimen perusteella.";    
    
    @GET
    @Path("/get")
    @Produces("application/json")
//    @Secured(Constants.ASIAKIRJAPALVELU_READ)
    @Transactional
    public Template template(@Context HttpServletRequest request) throws IOException,
            DocumentException {

        Template result = new Template();
        String[] fileNames = request.getParameter("templateFile").split(",");
        String language = request.getParameter("lang");
        String styleFile = request.getParameter("styleFile");
        if (styleFile != null) {
            result.setStyles(getStyle(styleFile));
        }

        List<TemplateContent> contents = new ArrayList<TemplateContent>();
        int order = 1;
        for (String file : fileNames) {
            String templateName = Utils.resolveTemplateName("/"+file+"_{LANG}.html", language);
            BufferedReader buff = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(templateName)));
            StringBuilder sb = new StringBuilder();

            String line = buff.readLine();
            while (line != null) {
                sb.append(line);
                line = buff.readLine();
            }
            TemplateContent content = new TemplateContent();
            content.setName(templateName);
            content.setContent(sb.toString());
            content.setOrder(order);
            contents.add(content);
            order++;
        }
        result.setContents(contents);
        Replacement replacement = new Replacement();
        replacement.setName("$letterBodyText");
        ArrayList<Replacement> rList = new ArrayList<Replacement>();
        rList.add(replacement);
        result.setReplacements(rList);
        return result;
    }

    @GET
    @Path("/getById")
    @Produces("application/json")
//    @Secured(Constants.ASIAKIRJAPALVELU_READ)
    @Transactional
    public Template templateByID(@Context HttpServletRequest request) throws IOException, DocumentException {        
       String templateId = request.getParameter("templateId");
       Long id = Long.parseLong(templateId);
       
       return templateService.findById(id);
    }

    @GET
    @Path("/getAvailableExamples")
    @Produces("application/json")
    @Transactional
    
    public List<Map<String, String>> templateExamples(@Context HttpServletRequest request) throws IOException, DocumentException {
        List<Map<String, String>> res = new ArrayList<Map<String, String>>();
       
       String[] templates = {"/hyvaksymiskirje_FI.json", "/hyvaksymiskirje_SV.json", "/jalkiohjauskirje_FI.json", "/jalkiohjauskirje_SV.json",
    		   				"/koekutsukirje_FI.json", "/koekutsukirje_SV.json", "/koekutsukirje_EN.json"};
       
       for (String template : templates) {
           Map<String, String> current = new HashMap<String, String>();
           BufferedReader buff = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(template)));
           StringBuilder sb = new StringBuilder();

           String line = buff.readLine();
           while (line != null) {
               sb.append(line);
               line = buff.readLine();
           }
           current.put("name", template);
           current.put("content", sb.toString());
           res.add(current);
       }
       
       return res;
    }

    
    
    @GET
    @Path("/getNames")
    @Produces("application/json")
//    @Secured(Constants.ASIAKIRJAPALVELU_READ)
    @Transactional
    
    @ApiOperation(value = TemplateNames, notes = TemplateNames2)	// SWAGGER
//  @ApiResponses({@ApiResponse(code = 200, message = getHistory200)
//  })
    
    public List<Map<String,String>> templateNames(@Context HttpServletRequest request) throws IOException, DocumentException {
       List<Map<String,String>> res = new ArrayList<Map<String,String>>();
       List<String> serviceResult = templateService.getTemplateNamesList();
       for (String s : serviceResult) {
           if (s != null && s.trim().length() > 0) {
               if (s.indexOf("::") > 0) {
                   Map<String,String> m = new HashMap<String,String>();
                   String[] sa = s.split("::");
                   m.put("name", sa[0]);
                   m.put("lang", sa[1]);
                   res.add(m);
               }
           }
       }
       return res;
    }

    @GET
    @Path("/getByName")
    @Produces("application/json")
//    @Secured(Constants.ASIAKIRJAPALVELU_READ)
    @Transactional
    
    // SWAGGER
    @ApiOperation(value = ApitemplateByName, notes = ApitemplateByName, response=Template.class)
    @ApiImplicitParams({												
    	@ApiImplicitParam(name = "templateName", value = "kirjepohjan nimi (hyvaksymiskirje, jalkiohjauskirje,..)",	required = true, dataType = "string", paramType = "query"),
    	@ApiImplicitParam(name = "languageCode", value = "kielikoodi (FI, SV, ...)", required = true, dataType = "string", paramType = "query"),
    	@ApiImplicitParam(name = "content", value = "YES, jos halutaan, että palautetaan myös viestin sisältö.", required = false, dataType = "string", paramType = "query")
    })   
    
    public Template templateByName(@Context HttpServletRequest request) throws IOException, DocumentException {       
       String templateName = request.getParameter("templateName");
       String languageCode = request.getParameter("languageCode");
       String content 	   = request.getParameter("content");		// If missing => content excluded
       boolean getContent = (content != null && "YES".equalsIgnoreCase(content));
       return templateService.getTemplateByName(templateName, languageCode, getContent);
    }

    @POST
    @Path("/store")
    @Consumes("application/json")
    @Produces("application/json")
//    @Secured(Constants.ASIAKIRJAPALVELU_CREATE_TEMPLATE)
    public Template store(Template template) throws IOException, DocumentException {
        templateService.storeTemplateDTO(template);
        return new Template();
    }
    

    @POST
    @Path("/storeDraft")
    @Consumes("application/json")
    @Produces("application/json")
//    @Secured(Constants.ASIAKIRJAPALVELU_CREATE_TEMPLATE)
    public Draft storeDraft(Draft draft) throws IOException, DocumentException {
        templateService.storeDraftDTO(draft);
        return new Draft();
    }
    
    
    @GET
    @Path("/getDraft")
    @Produces("application/json")
//    @Secured(Constants.ASIAKIRJAPALVELU_READ)
    @Transactional
    public Response getDraft(@Context HttpServletRequest request) throws IOException, DocumentException { 
        // Pick up the organization oid from request and check urer's rights to organization
        String oid = request.getParameter("oid");
        Response response = userRightsValidator.checkUserRightsToOrganization(oid); 
                
        // User isn't authorized to the organization
        if (response.getStatus() != 200) {
            return response;
        }
    	
        String templateName = request.getParameter("templateName");
        String languageCode = request.getParameter("languageCode");
//        String organizationOid = request.getParameter("oid");
        String applicationPeriod = request.getParameter("applicationPeriod");
        String fetchTarget = request.getParameter("fetchTarget");
        String tag = request.getParameter("tag");
        
        if (applicationPeriod==null) { 
        	applicationPeriod=""; 
        }
        if (fetchTarget==null) { 
        	fetchTarget=""; 
        }
        if (tag==null) { 
        	tag=""; 
        }

        Draft draft = new Draft();
        draft = templateService.findDraftByNameOrgTag(templateName, languageCode, oid, applicationPeriod, fetchTarget, tag);
        return Response.ok(draft).build();        
    }
    
    /**
     * http://localhost:8080/viestintapalvelu/api/v1/template/getTempHistory?templateName=hyvaksymiskirje&languageCode=FI&content=YES&oid=123456789&tag=par
     * 
     * @param request
     * @return
     * @throws IOException
     * @throws DocumentException
     */
    @GET
    @Path("/getTempHistory")
    @Produces("application/json")
//    @Secured(Constants.ASIAKIRJAPALVELU_READ)
    @Transactional
    public Response getTempHistory(@Context HttpServletRequest request) throws IOException, DocumentException {
        // Pick up the organization oid from request and check urer's rights to organization
        String oid = request.getParameter("oid");
        Response response = userRightsValidator.checkUserRightsToOrganization(oid); 
        
        // User isn't authorized to the organization
        if (response.getStatus() != 200) {
            return response;
        }

    	TemplateBundle bundle = new TemplateBundle();
    	
        String templateName = request.getParameter("templateName");
        String languageCode = request.getParameter("languageCode");
        String content 	   = request.getParameter("content");		// If missing => content excluded
        boolean getContent = (content != null && "YES".equalsIgnoreCase(content));
        
        bundle.setLatestTemplate(templateService.getTemplateByName(templateName, languageCode, getContent));
    	
        
        if ((oid!=null) && !("".equals(oid)) ) {
            
        	bundle.setLatestOrganisationReplacements(letterService.findReplacementByNameOrgTag(templateName, languageCode, oid, null));	
			
			String tag = request.getParameter("tag");
	        if ((tag!=null) && !("".equals(tag)) ) {
	        	bundle.setLatestOrganisationReplacementsWithTag(letterService.findReplacementByNameOrgTag(templateName, languageCode, oid, tag));
	        }
		}
		
		return Response.ok(bundle).build();
    }

    
    /**
     * http://localhost:8080/viestintapalvelu/api/v1/template/getHistory?templateName=hyvaksymiskirje&languageCode=FI&oid=123456789&tag=11111
     * @param request
     * @return
     * @throws IOException
     * @throws DocumentException
     */
    @GET
    @Path("/getHistory")
    @Produces("application/json")
//    @Secured(Constants.ASIAKIRJAPALVELU_READ)
    @Transactional
    
    // SWAGGER
    @ApiOperation(value = GetHistory, notes = GetHistory2, response = fi.vm.sade.viestintapalvelu.template.Replacement.class)
    @ApiResponses({@ApiResponse(code = 200, message = GetHistory200)
    })	        
    @ApiImplicitParams({												
    	@ApiImplicitParam(name = "templateName", value = "kirjepohjan nimi (hyvaksymiskirje, jalkiohjauskirje,..)",	required = true, dataType = "string", paramType = "query"),
    	@ApiImplicitParam(name = "languageCode", value = "kielikoodi (FI, SV, ...)", required = true, dataType = "string", paramType = "query"),
    	@ApiImplicitParam(name = "oid", value = "Organisaation Oid", required = true, dataType = "string", paramType = "query"),
    	@ApiImplicitParam(name = "tag", value = "Vapaa teksti tunniste", required = false, dataType = "string", paramType = "query"),
    })   
    
    public Response getHistory(@Context HttpServletRequest request) throws IOException, DocumentException {
        // Pick up the organization oid from request and check urer's rights to organization
        String oid = request.getParameter("oid");
        Response response = userRightsValidator.checkUserRightsToOrganization(oid); 
        
        // User isn't authorized to the organization
        if (response.getStatus() != 200) {
            return response;
        }

    	List<Map<String, Object>> history = new LinkedList<Map<String, Object>>();
    	
        String templateName = request.getParameter("templateName");
        String languageCode = request.getParameter("languageCode");
//        String content 	   = request.getParameter("content");		// If missing => content excluded
//        boolean getContent = (content != null && "YES".equalsIgnoreCase(content));
        boolean getContent = false;
               
        // OPH default template
        Template template = templateService.getTemplateByName(templateName, languageCode, getContent);
        
        Map<String, Object> templateRepl = new HashMap<String, Object>();
        templateRepl.put("name", "default");
        templateRepl.put("templateReplacements", template.getReplacements());
        history.add(templateRepl);       
                        
        if ((oid!=null) && !("".equals(oid)) ) {
            List<Replacement> templateReplacements = letterService.findReplacementByNameOrgTag(templateName, languageCode, oid, null);
            System.out.println(templateReplacements);
            
            if (templateReplacements != null && !templateReplacements.isEmpty()) {
                Map<String, Object> organisationRepl = new HashMap<String, Object>();
                organisationRepl.put("name", "organizationLatest");
                organisationRepl.put("templateReplacements", templateReplacements);
                history.add(organisationRepl);
            }
	
			// Latest LetterBatch replacements for that OrganisationOid with a tag
			String tag = request.getParameter("tag");
	        if ((tag!=null) && !("".equals(tag)) ) {
	        	templateReplacements = letterService.findReplacementByNameOrgTag(templateName,languageCode, oid, tag);
	            if (templateReplacements != null && !templateReplacements.isEmpty()) {
	                Map<String, Object> tagRepl = new HashMap<String, Object>();
	                tagRepl.put("name", "organizationLatestByTag");
	                tagRepl.put("templateReplacements", templateReplacements);
	                history.add(tagRepl);
	            }		        
	        }
        }
	     
        return Response.ok(history).build();
    }

    private String getStyle(String styleFile) throws IOException {
        BufferedReader buf = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/"+styleFile+".css")));
        StringBuilder sb = new StringBuilder();
        String line = buf.readLine();
        while (line != null) {
            sb.append(line);
            line = buf.readLine();
        }
        return sb.toString();
    }
    
}