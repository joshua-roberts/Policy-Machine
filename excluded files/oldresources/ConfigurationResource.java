package gov.nist.csd.pm.pep.oldresources;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.pep.requests.ConnectRequest;
import gov.nist.csd.pm.pep.requests.DataRequest;
import gov.nist.csd.pm.pep.requests.SetIntervalRequest;
import gov.nist.csd.pm.pep.response.ApiResponse;
import gov.nist.csd.pm.pdp.services.ConfigurationService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;

import static gov.nist.csd.pm.pep.response.ApiResponseCodes.SUCCESS;

@Path("/configuration")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ConfigurationResource {
    private ConfigurationService configService = new ConfigurationService();

    @GET
    public Response getConfiguration() throws ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {
        return new ApiResponse(configService.save()).toResponse();
    }

    @Path("connection")
    @POST
    public Response connect(ConnectRequest request) throws DatabaseException, ConfigurationException, SQLException, IOException, ClassNotFoundException, InvalidPropertyException {
        String database = request.getDatabase();
        String host = request.getHost();
        int port = request.getPort();
        String schema = request.getSchema();
        String username = request.getUsername();
        String password = request.getPassword();

        configService.connect(database, host, port, schema, username, password);

        return new ApiResponse(SUCCESS).toResponse();
    }

    @Path("interval")
    @POST
    public Response setInterval(SetIntervalRequest request) throws ConfigurationException {
        int interval = request.getInterval();

        configService.setInterval(interval);
        return new ApiResponse(interval).toResponse();
    }

    @Path("data")
    @POST
    public Response importData(@QueryParam("session") String session,
                               @QueryParam("process") long process,
                               ConnectRequest request) throws DatabaseException, ConfigurationException, InvalidNodeTypeException, InvalidPropertyException, AssignmentExistsException, NodeNotFoundException, NameInNamespaceNotFoundException, InvalidAssignmentException, SQLException, IOException, ClassNotFoundException, UnexpectedNumberOfNodesException, AssociationExistsException, PropertyNotFoundException, SessionDoesNotExistException, SessionUserNotFoundException, InvalidAssociationException {
        String host = request.getHost();
        int port = request.getPort();
        String schema = request.getSchema();
        String username = request.getUsername();
        String password = request.getPassword();

        configService.importData(host, port, schema, username, password, session, process);

        return new ApiResponse(SUCCESS).toResponse();
    }

    @Path("data/tables")
    @POST
    public Response getData(DataRequest request,
                            @QueryParam("session") String session,
                            @QueryParam("process") long process) throws PMException, IOException {
        String host = request.getHost();
        int port = request.getPort();
        String username = request.getUsername();
        String password = request.getPassword();
        String schema = request.getSchema();
        String tableName = request.getTable();

        return new ApiResponse(configService.getData(host, port, username, password, schema, tableName, session, process)).toResponse();
    }

    @Path("data/files")
    @POST
    public Response uploadFiles(String[] files,
                                @QueryParam("session") String session,
                                @QueryParam("process") long process) throws InvalidPropertyException, AssignmentExistsException, DatabaseException, InvalidKeySpecException, NodeNotFoundException, NodeIDExistsException, NodeNameExistsException, NodeNameExistsInNamespaceException, NoSuchAlgorithmException, NullNameException, ConfigurationException, NullTypeException, InvalidNodeTypeException, InvalidAssignmentException, IOException, ClassNotFoundException, SQLException, UnexpectedNumberOfNodesException, AssociationExistsException, NoBaseIDException, PropertyNotFoundException, InvalidAssociationException, NoSubjectParameterException, SessionDoesNotExistException, InvalidProhibitionSubjectTypeException, SessionUserNotFoundException, MissingPermissionException, PolicyClassNameExistsException {
        configService.uploadFiles(files, session, process);
        return new ApiResponse(SUCCESS).toResponse();
    }


    @Path("graph")
    @GET
    public Response getGraph(@QueryParam("session") String session,
                             @QueryParam("process") long process) throws NodeNotFoundException, InvalidNodeTypeException, InvalidPropertyException, ClassNotFoundException, SQLException, DatabaseException, IOException, SessionDoesNotExistException, SessionUserNotFoundException {
        return new ApiResponse(configService.getJsonGraph(session, process)).toResponse();
    }

    @Path("graph/users")
    @GET
    public Response getUserGraph(@QueryParam("session") String session,
                                 @QueryParam("process") long process) throws NodeNotFoundException, InvalidNodeTypeException, InvalidPropertyException, ClassNotFoundException, SQLException, DatabaseException, IOException, SessionDoesNotExistException, SessionUserNotFoundException {
        return new ApiResponse(configService.getUserGraph(session, process)).toResponse();
    }

    @Path("graph/objects")
    @GET
    public Response getObjGraph(@QueryParam("session") String session,
                                @QueryParam("process") long process) throws NodeNotFoundException, InvalidNodeTypeException, InvalidPropertyException, ClassNotFoundException, SQLException, DatabaseException, IOException, SessionDoesNotExistException, SessionUserNotFoundException {
        return new ApiResponse(configService.getObjGraph(session, process)).toResponse();
    }
}
