package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.pep.requests.CreateSessionRequest;
import gov.nist.csd.pm.pep.response.ApiResponse;
import gov.nist.csd.pm.pdp.services.SessionService;
import gov.nist.csd.pm.demos.ndac.translator.exceptions.PMAccessDeniedException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

@Path("/sessions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SessionResource {
    private SessionService sessionService = new SessionService();

    @POST
    public Response createSession(CreateSessionRequest request)
            throws NullNameException, NodeNameExistsInNamespaceException, NodeNameExistsException,
            NodeNotFoundException, DatabaseException, InvalidNodeTypeException,
            InvalidPropertyException, ConfigurationException, NullTypeException, NodeIDExistsException,
            AssignmentExistsException, InvalidKeySpecException, NoSuchAlgorithmException,
            PMAccessDeniedException, PropertyNotFoundException, InvalidAssignmentException, IOException, ClassNotFoundException, SQLException {
        String username = request.getUsername();
        String password = request.getPassword();

        return new ApiResponse(sessionService.createSession(username, password)).toResponse();
    }

    @Path("/{sessionID}")
    @DELETE
    public Response deleteSession(@PathParam("sessionID") String sessionId) throws ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {
        sessionService.deleteSession(sessionId);
        return new ApiResponse(ApiResponse.DELETE_SESSION_SUCCESS).toResponse();
    }

    @Path("/{sessionId}")
    @GET
    public Response getSessionUser(@PathParam("sessionId") String sessionId) throws SessionDoesNotExistException, IOException, SQLException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException, InvalidPropertyException {
        return new ApiResponse(sessionService.getSessionUser(sessionId)).toResponse();
    }
}
