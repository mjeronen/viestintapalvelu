package fi.vm.sade.ryhmasahkoposti.externalinterface.client;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fi.vm.sade.externalinterface.common.ObjectMapperProvider;
import fi.vm.sade.ryhmasahkoposti.api.dto.TemplateDTO;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HttpContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@RunWith(JUnit4.class)
public class ViestintapalveluRestClientTest {
    private final ViestintapalveluRestClient viestintapalveluRestClient = new ViestintapalveluRestClient("http://localhost/viestintapalvelu", new ObjectMapperProvider());
    private final HttpClient mockHttpClient = Mockito.mock(HttpClient.class);
    private final BasicHttpResponse response = new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "lol"));

    @Before
    public void setupMocks() throws IOException {
        ReflectionTestUtils.setField(viestintapalveluRestClient, "cachingClient", mockHttpClient);
        response.setEntity(new StringEntity(readFileFromClasspath("test_email_template.json")));
    }

    @Test
    public void templateCanBeParsedFromResponse() throws IOException {
        when(mockHttpClient.execute(any(HttpUriRequest.class), any(HttpContext.class))).thenReturn(response);

        TemplateDTO template = viestintapalveluRestClient.getTemplateContent("omattiedot", "FI", "email");

        assertEquals(new Long(17213), template.getId());
        assertEquals("omattiedot_email", template.getName());
        LocalDateTime t = LocalDateTime.parse("Tue May 22 10:51:39.161 EEST 2018", DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss.SSS z yyyy"));
        assertEquals(new Date(t.toInstant(ZoneOffset.ofHours(3)).toEpochMilli()), template.getTimestamp());
    }

    @Test
    public void templateRequestSetsClientSubSystemCode() throws IOException {
        when(mockHttpClient.execute(any(HttpUriRequest.class), any(HttpContext.class)))
            .then((Answer<HttpResponse>) invocation -> {
                HttpUriRequest request = invocation.getArgumentAt(0, HttpUriRequest.class);
                Header clientSubsystemCodeHeader = request.getLastHeader("clientSubSystemCode");
                assertNotNull(clientSubsystemCodeHeader);
                assertThat(clientSubsystemCodeHeader.getValue(), containsString("ryhmasahkoposti"));
                return response;
        });

        viestintapalveluRestClient.getTemplateContent("omattiedot", "FI", "email");
        verify(mockHttpClient, times(1)).execute(any(HttpUriRequest.class), any(HttpContext.class));
    }
    private String readFileFromClasspath(String filenameInMyPackage) throws IOException {
        return IOUtils.toString(new ClassPathResource(filenameInMyPackage, this.getClass()).getInputStream(), "UTF-8");
    }
}
