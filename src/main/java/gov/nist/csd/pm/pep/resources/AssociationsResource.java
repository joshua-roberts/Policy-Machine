package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.pep.requests.AssociationRequest;
import gov.nist.csd.pm.pep.response.ApiResponse;
import gov.nist.csd.pm.pdp.services.AnalyticsService;
import gov.nist.csd.pm.pdp.services.AssociationsService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.sql.SQLException;

import static gov.nist.csd.pm.model.Constants.*;

@Path("/associations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AssociationsResource {

    private AssociationsService associationsService = new AssociationsService();
    private AnalyticsService    permissionsService  = new AnalyticsService();

    @POST
    public Response createAssociation(AssociationRequest request,
                                      @QueryParam("session") String session,
                                      @QueryParam("process") long process)
            throws NodeNotFoundException, AssociationExistsException, ConfigurationException, DatabaseException, NoSubjectParameterException, MissingPermissionException, InvalidProhibitionSubjectTypeException, SessionUserNotFoundException, SessionDoesNotExistException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException, InvalidAssociationException {
        //PERMISSION CHECK
        //get user from username
        Node user = permissionsService.getSessionUser(session);

        //check user can create an association for the target and the subject
        //1. can create association for the target
        permissionsService.checkPermissions(user, process, request.getTargetID(), CREATE_ASSOCIATION);

        //TODO associations on UAs not yet implemented
        //2. can create an association for the subject
        //permissionsService.checkPermissions(user, process, request.getUaId(), ASSIGN);

        associationsService.createAssociation(request.getUaID(), request.getTargetID(), request.getOps());

        return new ApiResponse(ApiResponse.CREATE_ASSOCIATION_SUCCESS).toResponse();
    }

    @GET
    public Response getAssociations(@QueryParam("targetId") long targetId,
                                    @QueryParam("session") String session,
                                    @QueryParam("process") long process) throws NodeNotFoundException, InvalidPropertyException, NoSubjectParameterException, MissingPermissionException, InvalidProhibitionSubjectTypeException, SessionUserNotFoundException, ConfigurationException, SessionDoesNotExistException, ClassNotFoundException, SQLException, IOException, DatabaseException {
        if(targetId != 0) {
            return new ApiResponse(associationsService.getTargetAssociations(targetId)).toResponse();
        } else {
            return new ApiResponse(associationsService.getAssociations()).toResponse();
        }
    }

    @Path("/{targetId}")
    @PUT
    public Response updateAssociation(@PathParam("targetId") long targetId,
                                      AssociationRequest request,
                                      @QueryParam("session") String session,
                                      @QueryParam("process") long process)
            throws NodeNotFoundException, AssociationDoesNotExistException, ConfigurationException, DatabaseException, NoSubjectParameterException, MissingPermissionException, InvalidProhibitionSubjectTypeException, SessionUserNotFoundException, SessionDoesNotExistException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        //PERMISSION CHECK
        //get user from username
        Node user = permissionsService.getSessionUser(session);

        //check user can create an association for the target and the subject
        //1. can update association for the target
        permissionsService.checkPermissions(user, process, request.getTargetID(), UPDATE_ASSOCIATION);

        //TODO associations on UAs not yet implemented
        //2. can update an association for the subject
        //permissionsService.checkPermissions(user, process, request.getUaId(), UPDATE_ASSOCIATION);

        associationsService.updateAssociation(targetId, request.getUaID(), request.getOps());
        return new ApiResponse(ApiResponse.UPDATE_ASSOCIATION_SUCCESS).toResponse();
    }

    @Path("/{targetId}/subjects/{subjectId}")
    @DELETE
    public Response deleteAssociation(@PathParam("targetId") long targetId,
                                      @PathParam("subjectId") long uaId,
                                      @QueryParam("session") String session,
                                      @QueryParam("process") long process) throws NodeNotFoundException, NoUserParameterException, AssociationDoesNotExistException, ConfigurationException, DatabaseException, NoSubjectParameterException, MissingPermissionException, InvalidProhibitionSubjectTypeException, SessionUserNotFoundException, SessionDoesNotExistException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        //PERMISSION CHECK
        //get user from username
        Node user = permissionsService.getSessionUser(session);

        //check user can create an association for the target and the subject
        //1. can delete association for the target
        permissionsService.checkPermissions(user, process, targetId, DELETE_ASSOCIATION);

        //TODO associations on UAs not yet implemented
        //2. can delete an association for the subject
        //permissionsService.checkPermissions(user, process, uaId, DELETE_ASSOCIATION);

        associationsService.deleteAssociation(targetId, uaId);
        return new ApiResponse(ApiResponse.DELETE_ASSOCIATION_ASSOCIATION).toResponse();
    }


    @Path("/subjects/{subjectId}")
    @GET
    public Response getSubjectAssociations(@PathParam("subjectId") long subjectId,
                                           @QueryParam("session") String session,
                                           @QueryParam("process") long process)
            throws NodeNotFoundException, NoSubjectParameterException, MissingPermissionException, InvalidProhibitionSubjectTypeException, SessionUserNotFoundException, ConfigurationException, SessionDoesNotExistException, ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {
        //PERMISSION CHECK
        //get user from username
        Node user = permissionsService.getSessionUser(session);

        //check user can get associations that the subject is in
        permissionsService.checkPermissions(user, process, subjectId, GET_ASSOCIATIONS);

        return new ApiResponse(associationsService.getSubjectAssociations(subjectId)).toResponse();
    }
}
