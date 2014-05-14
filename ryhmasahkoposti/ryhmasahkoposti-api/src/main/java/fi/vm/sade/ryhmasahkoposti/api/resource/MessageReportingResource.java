package fi.vm.sade.ryhmasahkoposti.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;

import fi.vm.sade.ryhmasahkoposti.api.constants.RestConstants;
import fi.vm.sade.ryhmasahkoposti.api.constants.SecurityConstants;
import fi.vm.sade.ryhmasahkoposti.api.dto.ReportedMessageDTO;
import fi.vm.sade.ryhmasahkoposti.api.dto.ReportedMessagesDTO;

/**
 * REST-rajapinta ryhmäsähköpostien selailua varten
 * 
 * @author vehei1
 *
 */
@PreAuthorize(SecurityConstants.USER_IS_AUTHENTICATED)
@Path(RestConstants.PATH_REPORT_MESSAGES)
@Api(value = "reportMessages", description = "Ryhmäsähköpostin raportointi")
public interface MessageReportingResource {
    /**
     * Hakee käyttäjän ja hänen organisaationsa lähettämät ryhmäshköpostiviestit
     * 
     * @param organizationOid Organisaation oid-tunnus
     * @param nbrOfRows Haettavien ryhmäsähköpostiviestien lukumäärä
     * @param page Sivu, jolle halutaan siirtyä katselemaan viestejä
     * @param sortedBy Sarake, minkä mukaan lajittelu suoritetaan
     * @param order Lajittelujärjestys
     * @return Tiedot raportoitavista ryhmäsähköpostiviesteistä {@link ReportedMessagesDTO}
     */
    @Secured(SecurityConstants.READ)
    @Produces(MediaType.APPLICATION_JSON)
    @Path(RestConstants.PATH_REPORT_MESSAGES_LIST)
    @GET
    @ApiOperation(value = "Hakee käyttäjän ja hänen organisaationsa lähettämät ryhmäshköpostiviestit", 
        notes = "Hakee halutun määrän käyttäjän ja hänen organisaantionsa lähettämiä ryhmäshköpostiviestit. Haku voidaan"
            + "aloittaa tietystä kohtaa ja ne voidaan hakea lajiteltuna nousevasti tai laskevasti tietyn sarakkeen mukaan.", 
        response = ReportedMessagesDTO.class, responseContainer="List")
    @ApiResponses(value={@ApiResponse(code=500, message = "Internal service error tai ilmoitus liittymävirheestä")})
    public Response getReportedMessages(@ApiParam(value="Organisaation oid-tunnus", required=false) 
        @QueryParam(RestConstants.PARAM_ORGANIZATION_OID) String organizationOid, 
        @ApiParam(value="Haettavien rivien lukumäärä", required=true)
        @QueryParam(RestConstants.PARAM_NUMBER_OF_ROWS) Integer nbrOfRows, 
        @ApiParam(value="Sivu, mistä kohdasta haluttu määrä rivejä haetaan", required=true) 
        @QueryParam(RestConstants.PARAM_PAGE) Integer page, 
        @ApiParam(value="Taulun sarake, minkä mukaan tiedot lajitellaan", required=false)
        @QueryParam(RestConstants.PARAM_SORTED_BY) String sortedBy, 
        @ApiParam(value="Lajittelujärjestys", allowableValues="asc, desc" , required=false) 
        @QueryParam(RestConstants.PARAM_ORDER) String order);
	
	/**
	 * Hakee hakuparametrin mukaiset viestit käyttäjän ja hänen organisaationsa lähettämistä ryhmäshköpostiviesteistä
	 * 
	 * @param organizationOid Organisaation oid-tunnus
	 * @param hakuKentta Käyttäjän antama hakuparametri
     * @param nbrOfRows Haettavien ryhmäsähköpostiviestien lukumäärä
     * @param page Sivu, jolle halutaan siirtyä katselemaan viestejä
     * @param sortedBy Sarake, minkä mukaan lajittelu suoritetaan
     * @param order Lajittelujärjestys
	 * @return Tiedot raportoitavista ryhmäsähköpostiviesteistä {@link ReportedMessagesDTO}
	 */
	@Secured(SecurityConstants.READ)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(RestConstants.PATH_REPORT_MESSAGES_SEARCH)
	@GET
    @ApiOperation(value = "Hakee hakuparametrin mukaiset käyttäjän ja hänen organisaationsa lähettämät ryhmäshköpostiviestit", 
        notes = "Hakee halutun määrän  hakuparametrin mukaisia käyttäjän ja hänen organisaantionsa lähettämiä "
            + "ryhmäshköpostiviestejä. Haku voidaan aloittaa tietystä kohtaa ja ne voidaan hakea lajiteltuna nousevasti "
            + "tai laskevasti tietyn sarakkeen mukaan.", 
        response = ReportedMessagesDTO.class, responseContainer="List")
    @ApiResponses(value={@ApiResponse(code=500, message = "Internal service error tai ilmoitus liittymävirheestä")})
	public Response getReportedMessages(@ApiParam(value="Organisaation oid-tunnus", required=false) 
	    @QueryParam(RestConstants.PARAM_ORGANIZATION_OID) String organizationOid, 
	    @ApiParam(value="Annettu hakuparametri esim. sosiaaliturvatunnus", required=false)
	    @QueryParam(RestConstants.PARAM_SEARCH_ARGUMENT) String searchArgument, 
        @ApiParam(value="Haettavien rivien lukumäärä", required=true)
        @QueryParam(RestConstants.PARAM_NUMBER_OF_ROWS) Integer nbrOfRows, 
        @ApiParam(value="Sivu, mistä kohdasta haluttu määrä rivejä haetaan", required=true) 
        @QueryParam(RestConstants.PARAM_PAGE) Integer page, 
        @ApiParam(value="Taulun sarake, minkä mukaan tiedot lajitellaan", required=false)
        @QueryParam(RestConstants.PARAM_SORTED_BY) String sortedBy, 
        @ApiParam(value="Lajittelujärjestys", allowableValues="asc, desc" , required=false) 
        @QueryParam(RestConstants.PARAM_ORDER) String order);
	
	/**
	 * Hakee yksittäisen ryhmäsähköpostiviestin tiedot
	 * 
	 * @param Ryhmäsähköpostiviestin tunnus
	 * @return Raportoitavan ryhmäsähköpostin tiedot {@link ReportedMessageDTO}}
	 */
	@Secured(SecurityConstants.READ)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(RestConstants.PATH_REPORT_MESSAGE_VIEW)
	@GET
    @ApiOperation(value = "Hakee halutun ryhmäsähköpostiviestin tiedot", 
        notes = "Hakee avainta vastaavaa ryhmäsähköpostiviestin tiedot.", response = ReportedMessageDTO.class)
	@ApiResponses(value={@ApiResponse(code=500, message = "Internal service error")})
	public Response getReportedMessage(@ApiParam(value="Ryhmäsähköpostiviestin avain", required=true) 
	    @PathParam(RestConstants.PARAM_MESSAGE_ID) Long messageID);
	
	/**
	 * Hakee yksittäisen ryhmäsähköpostiviestin tiedot. Palauttaa tietyn määrän vastaanottajien tietoja
	 * 
	 * @param messageID Ryhmäsähköpostin tunnus
	 * @param nbrOfRows Palautettavien vastaanottajien lukumäärä
	 * @param page Sivu, jolle halutaan siirtyä katselemaan vastaanottajia
	 * @param sortedBy Kenttä, minkä mukaan lajitellaan
	 * @param order Nouseva (=asc) vai laskeva (= desc) lajittelujärjestys
	 * @return Yksittäisen ryhmäsähköpostiviestin tiedot ja vastaanottajien tiedot
	 */
	@Secured(SecurityConstants.READ)
    @Produces(MediaType.APPLICATION_JSON)
    @Path(RestConstants.PATH_REPORT_MESSAGE_VIEW_WITH_PAGING)
    @GET
    @ApiOperation(value = "Hakee halutun ryhmäsähköpostiviestin ja viestin vastaanottajien tiedot", 
        notes = "Hakee avainta vastaavaa ryhmäsähköpostiviestin ja vastaanottajien tiedot. Palauttaa halutun määrän "
            + "vastaanottajia lajiteltuna halutun sarakkeen mukaisesti", response = ReportedMessageDTO.class)
    @ApiResponses(value={@ApiResponse(code=500, message = "Internal service error tai liittymävirhe")})
    public Response getReportedMessageAndRecipients(@ApiParam(value="Ryhmäsähköpostiviestin avain", required=true) 
        @PathParam(RestConstants.PARAM_MESSAGE_ID) Long messageID, 
        @ApiParam(value="Haettavien rivien lukumäärä", required=true)
        @QueryParam(RestConstants.PARAM_NUMBER_OF_ROWS) Integer nbrOfRows, 
        @ApiParam(value="Sivu, mistä kohdasta haluttu määrä rivejä haetaan", required=true) 
        @QueryParam(RestConstants.PARAM_PAGE) Integer page, 
        @ApiParam(value="Taulun sarake, minkä mukaan tiedot lajitellaan", required=false)
        @QueryParam(RestConstants.PARAM_SORTED_BY) String sortedBy, 
        @ApiParam(value="Lajittelujärjestys", allowableValues="asc, desc" , required=false) 
        @QueryParam(RestConstants.PARAM_ORDER) String order);

	/**
     * Hakee yksittäisen ryhmäsähköpostiviestin tiedot. Palauttaa vastaanottajien tiedot, joille lähetys epäonnistui
     * 
     * @param messageID Ryhmäsähköpostin tunnus
     * @param nbrOfRows Palautettavien vastaanottajien lukumäärä
     * @param page Sivu, jolle halutaan siirtyä katselemaan vastaanottajia
     * @param sortedBy Kenttä, minkä mukaan lajitellaan
     * @param order Nouseva (=asc) vai laskeva (= desc) lajittelujärjestys
     * @return Yksittäisen ryhmäsähköpostiviestin tiedot ja vastaanottajien tiedot, joille lähetys epäonnistui
     */
    @Secured(SecurityConstants.READ)
    @Produces(MediaType.APPLICATION_JSON)
    @Path(RestConstants.PATH_REPORT_MESSAGE_FAILED_VIEW_WITH_PAGING)
    @GET
    @ApiOperation(value = "Hakee ryhmäsähköpostiviestin ja viestin vastaanottajien tiedot, joille lähetys epäonnistui", 
        notes = "Hakee avainta vastaavaa ryhmäsähköpostiviestin ja vastaanottajien tiedot, joille lähetys epäonnistui. "
            + "Palauttaa halutun määrän vastaanottajia lajiteltuna halutun sarakkeen mukaisesti", 
        response = ReportedMessageDTO.class)
    @ApiResponses(value={@ApiResponse(code=500, message = "Internal service error tai liittymävirhe")})
    public Response getReportedMessageAndRecipientsSendingUnsuccesful(
        @ApiParam(value="Ryhmäsähköpostiviestin avain", required=true)
        @PathParam(RestConstants.PARAM_MESSAGE_ID) Long messageID, 
        @ApiParam(value="Haettavien rivien lukumäärä", required=true)
        @QueryParam(RestConstants.PARAM_NUMBER_OF_ROWS) Integer nbrOfRows, 
        @ApiParam(value="Sivu, mistä kohdasta haluttu määrä rivejä haetaan", required=true) 
        @QueryParam(RestConstants.PARAM_PAGE) Integer page, 
        @ApiParam(value="Taulun sarake, minkä mukaan tiedot lajitellaan", required=false)
        @QueryParam(RestConstants.PARAM_SORTED_BY) String sortedBy, 
        @ApiParam(value="Lajittelujärjestys", allowableValues="asc, desc" , required=false) 
        @QueryParam(RestConstants.PARAM_ORDER) String order);
}