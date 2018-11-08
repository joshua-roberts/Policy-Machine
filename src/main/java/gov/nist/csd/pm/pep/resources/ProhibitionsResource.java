package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.prohibitions.Prohibition;
import gov.nist.csd.pm.pep.requests.CreateProhibitionRequest;
import gov.nist.csd.pm.pep.response.ApiResponse;
import gov.nist.csd.pm.pdp.services.ProhibitionsService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/prohibitions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProhibitionsResource {

    @POST
    public Response createProhibition(CreateProhibitionRequest request,
                                      @QueryParam("session") String session,
                                      @QueryParam("process") long process) throws ProhibitionNameExistsException, DatabaseException, NullNameException, LoadConfigException, LoaderException, MissingPermissionException, NodeNotFoundException, SessionDoesNotExistException, InvalidProhibitionSubjectTypeException {
        ProhibitionsService prohibitionsService = new ProhibitionsService(session, process);
        Prohibition prohibition = new Prohibition(request.getName(), request.getSubject(), request.getNodes(), request.getOperations(), request.isIntersection());
        prohibitionsService.createProhibition(prohibition);
        return ApiResponse.Builder.success().message(ApiResponse.CREATE_PROHIBITION_SUCCESS).build();
    }

    @GET
    public Response getProhibitions(@QueryParam("session") String session,
                                    @QueryParam("process") long process) throws DatabaseException, LoadConfigException, LoaderException, InvalidProhibitionSubjectTypeException {
        ProhibitionsService prohibitionsService = new ProhibitionsService(session, process);
        List<Prohibition> prohibitions = prohibitionsService.getProhibitions();
        return ApiResponse.Builder.success().entity(prohibitions).build();
    }

    @Path("/{prohibitionName}")
    @GET
    public Response getProhibition(@PathParam("prohibitionName") String prohibitionName,
                                   @QueryParam("session") String session,
                                   @QueryParam("process") long process) throws LoaderException, ProhibitionDoesNotExistException, LoadConfigException, DatabaseException, InvalidProhibitionSubjectTypeException {
        ProhibitionsService prohibitionsService = new ProhibitionsService(session, process);
        return ApiResponse.Builder.success().entity(prohibitionsService.getProhibition(prohibitionName)).build();
    }

    @Path("/{prohibitionName}")
    @PUT
    public Response updateProhibition(@PathParam("prohibitionName") String prohibitionName,
                                      CreateProhibitionRequest request,
                                      @QueryParam("session") String session,
                                      @QueryParam("process") long process)
            throws DatabaseException, InvalidProhibitionSubjectTypeException, LoadConfigException, LoaderException {
        ProhibitionsService prohibitionsService = new ProhibitionsService(session, process);
        Prohibition prohibition = new Prohibition(request.getName(), request.getSubject(), request.getNodes(), request.getOperations(), request.isIntersection());
        prohibitionsService.updateProhibition(prohibition);
        return ApiResponse.Builder.success().message(ApiResponse.UPDATE_PROHIBITION_SUCCESS).build();
    }

    @Path("/{prohibitionName}")
    @DELETE
    public Response deleteProhibition(@PathParam("prohibitionName") String prohibitionName,
                                      @QueryParam("session") String session,
                                      @QueryParam("process") long process)
            throws DatabaseException, ProhibitionDoesNotExistException, LoadConfigException, InvalidProhibitionSubjectTypeException, LoaderException {
        ProhibitionsService prohibitionsService = new ProhibitionsService(session, process);
        prohibitionsService.deleteProhibition(prohibitionName) ;
        return ApiResponse.Builder.success().message(ApiResponse.DELETE_PROHIBITION_SUCCESS).build();
    }
}
