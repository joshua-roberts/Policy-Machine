package gov.nist.csd.pm.demos.ndac.pmhealth.resources;

import gov.nist.csd.pm.demos.ndac.pmhealth.dao.DAO;
import gov.nist.csd.pm.pep.response.ApiResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;

@Path("/demos/pmhealth/sessions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SessionsResource {

    private DAO dao;
    public SessionsResource() throws PropertyVetoException, SQLException, ClassNotFoundException {
        dao = DAO.getDao();
    }

    @Path("/{sessionId}")
    @DELETE
    public Response logout(@PathParam("sessionId") String sessionId) {
        dao.deleteSession(sessionId);
        return ApiResponse.Builder.success().build();
    }

    @Path("/{sessionId}")
    @GET
    public Response getSession(@PathParam("sessionId") String sessionId) throws SQLException, IOException {
        return ApiResponse.Builder.success().entity(dao.getPatient(sessionId)).build();
    }

    @Path("/{sessionId}/user")
    @GET
    public Response getSessionUser(@PathParam("sessionId") String sessionId) throws SQLException, IOException {
        return ApiResponse.Builder.success().entity(dao.getSessionUsername(sessionId)).build();
    }
}
