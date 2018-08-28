package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.pep.requests.ConnectRequest;
import gov.nist.csd.pm.pep.requests.ImportEntitiesRequest;
import gov.nist.csd.pm.pep.requests.ImportFilesRequest;
import gov.nist.csd.pm.pep.response.ApiResponse;
import gov.nist.csd.pm.pep.services.ImportService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;

import static gov.nist.csd.pm.model.Constants.SUCCESS;

@Path("/import")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ImportResource {

    private ImportService importService = new ImportService();

    @Path("files")
    @POST
    public Response importFiles(ImportFilesRequest request,
                                @QueryParam("session") String session,
                                @QueryParam("process") long process) throws InvalidPropertyException, AssignmentExistsException, InvalidNodeTypeException, NodeNotFoundException, ClassNotFoundException, NodeIdExistsException, NodeNameExistsException, NodeNameExistsInNamespaceException, DatabaseException, ConfigurationException, SQLException, NullNameException, IOException, NullTypeException, InvalidAssignmentException, UnexpectedNumberOfNodesException, AssociationExistsException, NoBaseIdException, PropertyNotFoundException, NoSubjectParameterException, SessionDoesNotExistException, InvalidProhibitionSubjectTypeException, SessionUserNotFoundException, MissingPermissionException {
        importService.importFiles(request.getFiles(), request.getSource(), session, process);
        return new ApiResponse("Successfully imported " + request.getFiles().length + " files from " + request
                .getSource()).toResponse();
    }

    @Path("entities")
    @POST
    public Response importEntities(ImportEntitiesRequest request) throws InvalidPropertyException, AssignmentExistsException, InvalidNodeTypeException, NodeNotFoundException, ClassNotFoundException, NodeIdExistsException, NodeNameExistsException, NodeNameExistsInNamespaceException, DatabaseException, ConfigurationException, SQLException, NullNameException, IOException, NullTypeException, InvalidAssignmentException, UnexpectedNumberOfNodesException, AssociationExistsException, NoBaseIdException, PropertyNotFoundException {
        importService.importEntities(request.getKind(), request.getEntities());

        return null;
    }

    @Path("sql")
    @POST
    public Response importData(@QueryParam("session") String session,
                               @QueryParam("process") long process,
                               ConnectRequest request) throws DatabaseException, ConfigurationException, InvalidNodeTypeException, InvalidPropertyException, AssignmentExistsException, NodeNotFoundException, NameInNamespaceNotFoundException, InvalidAssignmentException, SQLException, IOException, ClassNotFoundException, NodeIdExistsException, NodeNameExistsException, NodeNameExistsInNamespaceException, NullTypeException, NullNameException, UnexpectedNumberOfNodesException, AssociationExistsException, NoBaseIdException, PropertyNotFoundException, SessionDoesNotExistException, SessionUserNotFoundException {
        String host = request.getHost();
        int port = request.getPort();
        String schema = request.getSchema();
        String username = request.getUsername();
        String password = request.getPassword();

        importService.importSql(host, port, schema, username, password, session, process);

        return new ApiResponse(SUCCESS).toResponse();
    }

}
