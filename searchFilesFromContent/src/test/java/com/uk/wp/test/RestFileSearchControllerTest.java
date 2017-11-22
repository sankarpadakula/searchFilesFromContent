package com.uk.wp.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.Arrays;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.plugins.providers.jackson.ResteasyJacksonProvider;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import com.uk.wp.so.FileListSO;
import com.uk.wp.so.FileRequest;

public class RestFileSearchControllerTest {

  private static Client client;
  private String REST_SERVICE_URL = "http://localhost:8080/searchservice/rest/";

    @BeforeClass
    public static void beforeClass() throws Exception {
      ResteasyProviderFactory instance=ResteasyProviderFactory.getInstance();
      RegisterBuiltin.register(instance);
      instance.registerProvider(ResteasyJacksonProvider.class);
      client = ClientBuilder.newClient();
    }

    /*@AfterClass
    public static void afterClass() throws Exception {
        server.close();
    }
*/
    @Test
    public void testgetFileContentSimpleBody() throws Exception {
      Response response = client.target(REST_SERVICE_URL+"filecontents?match=private").request().get();
      assertEquals(200, response.getStatus());
      FileListSO fileListSO = (FileListSO)convertToJson(response.readEntity(String.class), FileListSO.class);
      assertNotNull(fileListSO.getFiles());
    }

    @Test
    public void testgetFileContentWithNoBody() throws Exception {
      Response response = client.target(REST_SERVICE_URL+"filecontents?").request().get();
      assertEquals(400, response.getStatus());
      String body = response.readEntity(String.class);
      assertEquals("Missing parameters", body);
    }
    
    @Test
    public void testgetFileContentWithMisMatchBody() throws Exception {
      Response response = client.target(REST_SERVICE_URL+"filecontents?match=zyx").request().get();
      assertEquals(200, response.getStatus());
      String body = response.readEntity(String.class);
      assertEquals("No matching content found", body);
    }
    
    @Test
    public void testRefresh() throws Exception {
      Response response = client.target(REST_SERVICE_URL+"refresh").request().get();
      assertEquals(200, response.getStatus());
      String body = response.readEntity(String.class);
      assertEquals("", body);
    }
    
    @Test
    public void testFileContentSimpleBody() throws Exception {
      FileRequest request = new FileRequest();
      request.setMatch(Arrays.asList("private"));
      Response response = client.target(REST_SERVICE_URL+"filecontents").request().post(Entity.json(request));
      assertEquals(200, response.getStatus());
      FileListSO fileListSO = (FileListSO)convertToJson(response.readEntity(String.class), FileListSO.class);
      assertNotNull(fileListSO.getFiles());
    }
    
    @Test
    public void testPostFileContentMultipleBody() throws Exception {
      FileRequest request = new FileRequest();
      request.setMatch(Arrays.asList("private", "Girl"));
      Response response = client.target(REST_SERVICE_URL+"filecontents").request().post(Entity.json(request));
      assertEquals(200, response.getStatus());
      FileListSO fileListSO = (FileListSO)convertToJson(response.readEntity(String.class), FileListSO.class);
      assertNotNull(fileListSO.getFiles());
    }
    
    @Test
    public void testPostFileContentNoBody() throws Exception {
      FileRequest request = new FileRequest();
      Response response = client.target(REST_SERVICE_URL+"filecontents").request().post(Entity.json(request));
      assertEquals(400, response.getStatus());
      String body = response.readEntity(String.class);
      assertEquals("Missing parameters", body);
    }
    
    @Test
    public void testPostFileContentMisMatchBody() throws Exception {
      FileRequest request = new FileRequest();
      request.setMatch(Arrays.asList("zyx", "Girl"));
      Response response = client.target(REST_SERVICE_URL+"filecontents").request().post(Entity.json(request));
      assertEquals(200, response.getStatus());
      String body = response.readEntity(String.class);
      assertEquals("No matching content found", body);
    }
    
    private Object convertToJson(String response, Class className) throws IOException, JsonParseException, JsonMappingException {
      ObjectMapper mapper = new ObjectMapper();
      mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
      return mapper.readValue(response, className);
    }
}

