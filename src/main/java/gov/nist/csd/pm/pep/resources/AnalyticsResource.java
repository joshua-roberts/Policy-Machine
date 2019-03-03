package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.common.exceptions.*;

import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.pdp.services.AnalyticsService;
import gov.nist.csd.pm.pdp.services.SessionsService;
import gov.nist.csd.pm.pep.response.ApiResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Set;

@Path("/analytics")
public class AnalyticsResource {
    @Path("/pos")
    @GET
    public Response getPos(@QueryParam("session") String session,
                           @QueryParam("process") long process) throws PMException {
        AnalyticsService analyticsService = new AnalyticsService(new SessionsService().getSessionUserID(session), process);
        return ApiResponse.Builder
                .success()
                .entity(analyticsService.getPos())
                .build();
    }

    @Path("/{targetID}")
    @GET
    public Response getPermissions(@PathParam("targetID") long targetID,
                                   @QueryParam("session") String session,
                                   @QueryParam("process") long process) throws PMException {
        AnalyticsService analyticsService = new AnalyticsService(new SessionsService().getSessionUserID(session), process);
        Set<String> permissions = analyticsService.getPermissions(targetID);
        return ApiResponse.Builder
                .success()
                .entity(permissions)
                .build();
    }

    @Path("/explain")
    @GET
    public Response explain(@QueryParam("user") long userID,
                            @QueryParam("target") long targetID,
                            @QueryParam("session") String session,
                            @QueryParam("process") long process) throws PMException {
        AnalyticsService service = new AnalyticsService(new SessionsService().getSessionUserID(session), process);
        System.out.println(service.explain(userID, targetID));
        return null;
    }
}
