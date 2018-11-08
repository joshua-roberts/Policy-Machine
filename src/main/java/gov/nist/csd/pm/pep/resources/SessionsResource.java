package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.exceptions.LoaderException;
import gov.nist.csd.pm.pdp.services.SessionsService;
import gov.nist.csd.pm.pep.requests.CreateSessionRequest;
import gov.nist.csd.pm.pep.response.ApiResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/sessions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class SessionsResource {

    @POST
    public Response createSession(CreateSessionRequest request) throws DatabaseException, HashingUserPasswordException, SessionDoesNotExistException, NodeNotFoundException, LoadConfigException, PMAuthenticationException, LoaderException, MissingPermissionException, InvalidProhibitionSubjectTypeException {
        String username = request.getUsername();
        String password = request.getPassword();

        SessionsService sessionsService = new SessionsService();
        String sessionID = sessionsService.createSession(username, password);

        return ApiResponse.Builder
                .success()
                .entity(sessionID)
                .build();
    }

    @Path("/{session}")
    @DELETE
    public Response deleteSession(@PathParam("session") String session) throws DatabaseException, LoadConfigException, LoaderException, InvalidProhibitionSubjectTypeException {
        SessionsService sessionsService = new SessionsService();
        sessionsService.deleteSession(session);
        return ApiResponse.Builder
                .success()
                .message(ApiResponse.DELETE_SESSION_SUCCESS)
                .build();
    }

    @Path("/{session}")
    @GET
    public Response getSessionUser(@PathParam("session") String session) throws LoaderException, SessionDoesNotExistException, LoadConfigException, DatabaseException, InvalidProhibitionSubjectTypeException {
        SessionsService sessionsService = new SessionsService();
        long userID = sessionsService.getSessionUserID();
        return ApiResponse.Builder
                .success()
                .entity(userID)
                .build();
    }
}
