package gov.nist.csd.pm.demos.ndac.pmhealth.resources;

import gov.nist.csd.pm.demos.ndac.pmhealth.dao.DAO;
import gov.nist.csd.pm.pep.response.ApiResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;

@Path("/demos/pmhealth/links")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LinksResource {
    private DAO dao;
    public LinksResource() throws PropertyVetoException, SQLException, ClassNotFoundException {
        dao = DAO.getDao();
    }

    @GET
    public Response getLinks(@QueryParam("user") String user, @QueryParam("session") String session) throws SQLException, IOException {
        return ApiResponse.Builder.success().entity(dao.getLinks(user)).build();
    }

    @Path("/home")
    @GET
    public Response getHome(@QueryParam("user") String user) throws IOException, SQLException {
        return ApiResponse.Builder.success().entity(dao.getHome(user)).build();
    }

    @Path("/actions")
    @GET
    public Response getActions(@QueryParam("user") String user) throws IOException, SQLException {
        return ApiResponse.Builder.success().entity(dao.getActions(user)).build();
    }
}
