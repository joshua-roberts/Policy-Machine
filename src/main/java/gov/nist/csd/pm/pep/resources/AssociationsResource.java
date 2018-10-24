package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.OldNode;
import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.graph.relationships.NGACAssociation;
import gov.nist.csd.pm.pdp.PDP;
import gov.nist.csd.pm.pep.requests.AssociationRequest;
import gov.nist.csd.pm.pep.response.ApiResponse;
import gov.nist.csd.pm.pip.loader.LoaderException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashSet;

import static gov.nist.csd.pm.model.constants.Operations.UPDATE_ASSOCIATION;

@Path("/associations")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AssociationsResource {

    @POST
    public Response createAssociation(AssociationRequest request,
                                      @QueryParam("session") String session,
                                      @QueryParam("process") long process) throws DatabaseException, LoadConfigException, LoaderException, NodeNotFoundException, NullOperationsException, MissingPermissionException, InvalidNodeTypeException, SessionDoesNotExistException, InvalidAssociationException {
        PDP pdp = new PDP(session, process);

        //check parameters
        long sourceID = request.getSourceID();
        if(pdp.exists(sourceID)) {
            throw new NodeNotFoundException(sourceID);
        }
        long targetID = request.getTargetID();
        if(pdp.exists(targetID)) {
            throw new NodeNotFoundException(targetID);
        }
        HashSet<String> operations = request.getOperations();
        if(operations == null) {
            throw new NullOperationsException();
        }

        Node sourceNode = pdp.getNode(sourceID);
        Node targetNode = pdp.getNode(targetID);

        NGACAssociation.checkAssociation(sourceNode.getType(), targetNode.getType());

        pdp.associate(sourceID, targetID, operations);

        return ApiResponse.Builder
                .success()
                .message(ApiResponse.CREATE_ASSOCIATION_SUCCESS)
                .build();
    }

    @GET
    public Response getAssociations(@QueryParam("targetId") long targetId,
                                    @QueryParam("session") String session,
                                    @QueryParam("process") long process) throws LoaderException, SessionDoesNotExistException, LoadConfigException, MissingPermissionException, DatabaseException, NoIDException {
        PDP pdp = new PDP(session, process);

        if(targetId == 0) {
            throw new NoIDException();
        }

        return ApiResponse.Builder
                .success()
                .entity(pdp.getTargetAssociations(targetId))
                .build();
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
        OldNode user = permissionsService.getSessionUserID(session);

        //check user can create an association for the target and the subject
        //1. can update association for the target
        permissionsService.checkPermissions(user, process, request.getTargetID(), UPDATE_ASSOCIATION);

        //TODO associations on UAs not yet implemented
        //2. can update an association for the subject
        //permissionsService.checkPermissions(user, process, request.getUaId(), UPDATE_ASSOCIATION);

        associationsService.updateAssociation(targetId, request.getSourceID(), request.getOperations());
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
        OldNode user = permissionsService.getSessionUserID(session);

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
        OldNode user = permissionsService.getSessionUserID(session);

        //check user can get associations that the subject is in
        permissionsService.checkPermissions(user, process, subjectId, GET_ASSOCIATIONS);

        return new ApiResponse(associationsService.getSubjectAssociations(subjectId)).toResponse();
    }
}
