package com.uk.wp.rest;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.log4j.Logger;

import com.uk.wp.service.FileSearchservice;
import com.uk.wp.service.impl.FileSearchServiceImpl;
import com.uk.wp.so.FileListSO;
import com.uk.wp.so.FileRequest;

@Path("/rest")
public class FileSearchController {

  final static Logger LOGGER = Logger.getLogger(FileSearchController.class);

  FileSearchservice service = new FileSearchServiceImpl();

  @GET
  @Path("/filecontents")
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
  public Response getFileContent(@QueryParam("match") String textToMatch) throws FileNotFoundException {
    if (textToMatch == null) {
      return Response.status(400).entity("Missing parameters").build();
    }
    List<String> words = Arrays.asList(textToMatch.split("\\s+"));
    return searchMatchings(words);
  }

  @POST
  @Path("/filecontents")
  @Consumes({ MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN })
  public Response fileContent(FileRequest textToMatch) throws FileNotFoundException {
    if (textToMatch.getMatch() == null || textToMatch.getMatch().isEmpty()) {
      return Response.status(400).entity("Missing parameters").build();
    }
    return searchMatchings(textToMatch.getMatch());
  }

  @GET
  @Path("/refresh")
  @Produces({ MediaType.TEXT_PLAIN })
  public Response refresh() throws FileNotFoundException {
    service.refreshIndex();
    return Response.ok().build();
  }

  private Response searchMatchings(List<String> words) {
    ResponseBuilder response = null;
    FileListSO so = null;
    try {
      so = service.fileContentListBycontent(words);
    } catch (IOException e) {
      response = Response.serverError();
    }

    if (so == null || so.getFiles() == null || so.getFiles().isEmpty()) {
      response = Response.noContent().entity("No matching content found");
    } else {
      response = Response.ok(so);
      // response.header("Content-Disposition", "attachement; filename=" + so.getFileName());
    }
    return response.build();
  }
}