package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.pep.requests.CreateProhibitionRequest;
import gov.nist.csd.pm.pep.response.ApiResponse;
import gov.nist.csd.pm.pdp.services.ProhibitionsService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;

@Path("/prohibitions")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProhibitionsResource {

    private ProhibitionsService prohibitionsService = new ProhibitionsService();

    @POST
    public Response createProhibition(CreateProhibitionRequest request)
            throws ProhibitionNameExistsException,
            DatabaseException, ConfigurationException, NullNameException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        return new ApiResponse(prohibitionsService.createProhibition(request.getName(), request.getOperations(), request.isIntersection(), request.getResources(), request.getSubject())).toResponse();
    }

    @GET
    public Response getProhibitions(@QueryParam("subjectID") long subjectID, @QueryParam("resourceID") long resourceID) throws ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {
        return new ApiResponse(prohibitionsService.getProhibitions(subjectID, resourceID)).toResponse();
    }

    @Path("/{prohibitionName}")
    @GET
    public Response getProhibition(@PathParam("prohibitionName") String prohibitionName)
            throws ProhibitionDoesNotExistException, ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {
        return new ApiResponse(prohibitionsService.getProhibition(prohibitionName)).toResponse();
    }

    @Path("/{prohibitionName}")
    @PUT
    public Response updateProhibition(@PathParam("prohibitionName") String prohibitionName,
                                      CreateProhibitionRequest request)
            throws DatabaseException, ProhibitionDoesNotExistException,
            NodeNotFoundException, InvalidProhibitionSubjectTypeException, ProhibitionResourceExistsException,
            ConfigurationException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        return new ApiResponse(prohibitionsService.updateProhibition(request.getName(), request.isIntersection(), request.getOperations(), request.getResources(), request.getSubject())).toResponse();
    }

    @Path("/{prohibitionName}")
    @DELETE
    public Response deleteProhibition(@PathParam("prohibitionName") String prohibitionName)
            throws DatabaseException, ProhibitionDoesNotExistException, ConfigurationException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        prohibitionsService.deleteProhibition(prohibitionName) ;
        return new ApiResponse(ApiResponse.DELETE_PROHIBITION_SUCCESS).toResponse();
    }
}
