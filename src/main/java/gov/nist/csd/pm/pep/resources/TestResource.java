package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.pdp.services.TestService;
import gov.nist.csd.pm.pep.response.ApiResponse;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

import static gov.nist.csd.pm.pep.response.ApiResponseCodes.SUCCESS;

@Path("/tests")
public class TestResource {

    private TestService service = new TestService();

    @Path("/graph")
    @GET
    public Response getTestGraph(@QueryParam("session") String session,
                                 @QueryParam("process") long process) throws ClassNotFoundException, SQLException, DatabaseException, IOException, InvalidPropertyException, InvalidAssignmentException, NoSubjectParameterException, NoSuchAlgorithmException, ConfigurationException, InvalidNodeTypeException, SessionDoesNotExistException, InvalidProhibitionSubjectTypeException, MissingPermissionException, UnexpectedNumberOfNodesException, NullNameException, NullTypeException, NodeNameExistsException, NodeIDExistsException, PropertyNotFoundException, InvalidAssociationException, InvalidKeySpecException, SessionUserNotFoundException, NodeNotFoundException, AssignmentExistsException, AssociationExistsException, NodeNameExistsInNamespaceException, PolicyClassNameExistsException {
        return new ApiResponse(service.getTestGraph(session, process)).toResponse();
    }

    @Path("/graph/{uuid}")
    @DELETE
    public Response deleteTestGraph(@PathParam("uuid") String uuid) throws DatabaseException, IOException, SQLException, InvalidPropertyException, InvalidNodeTypeException, ClassNotFoundException, NodeNotFoundException {
        service.deleteTestGraph(uuid);
        return new ApiResponse(SUCCESS).toResponse();
    }
}
