package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.exceptions.LoaderException;
import gov.nist.csd.pm.pdp.services.AnalyticsService;
import gov.nist.csd.pm.pep.response.ApiResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.HashSet;

@Path("/analytics")
public class AnalyticsResource {
    @Path("/pos")
    @GET
    public Response getPos(@QueryParam("session") String session,
                           @QueryParam("process") long process) throws SessionDoesNotExistException, NodeNotFoundException, LoaderException, LoadConfigException, MissingPermissionException, DatabaseException, InvalidNodeTypeException, InvalidProhibitionSubjectTypeException {
        AnalyticsService analyticsService = new AnalyticsService(session, process);
        return ApiResponse.Builder
                .success()
                .entity(analyticsService.getPos())
                .build();
    }

    @Path("/{targetID}")
    @GET
    public Response getPermissions(@PathParam("targetID") long targetID,
                                   @QueryParam("session") String session,
                                   @QueryParam("process") long process) throws SessionDoesNotExistException, LoadConfigException, NodeNotFoundException, MissingPermissionException, LoaderException, DatabaseException, InvalidProhibitionSubjectTypeException {
        AnalyticsService analyticsService = new AnalyticsService(session, process);
        HashSet<String> permissions = analyticsService.getPermissions(targetID);
        return ApiResponse.Builder
                .success()
                .entity(permissions)
                .build();
    }
}
