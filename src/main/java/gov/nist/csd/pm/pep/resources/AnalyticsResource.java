package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.pdp.analytics.PmAnalyticsEntry;
import gov.nist.csd.pm.model.graph.Node;
import gov.nist.csd.pm.model.graph.NodeType;
import gov.nist.csd.pm.pep.response.ApiResponse;
import gov.nist.csd.pm.pep.services.AnalyticsService;
import gov.nist.csd.pm.pep.services.NodeService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("/analytics")
public class AnalyticsResource {
    private AnalyticsService analyticsService = new AnalyticsService();
    private NodeService      nodeService      = new NodeService();

    @Path("/{var1:target}/children")
    @GET
    public Response getAccessibleChildren(@PathParam("var1") PathSegment targetPs,
                                          @QueryParam("permissions") String permissions,
                                          @QueryParam("session") String session,
                                          @QueryParam("process") long process) throws SQLException, IOException, UnexpectedNumberOfNodesException, ClassNotFoundException, InvalidPropertyException, DatabaseException, InvalidNodeTypeException, SessionDoesNotExistException, SessionUserNotFoundException, NodeNotFoundException, NoUserParameterException, ConfigurationException, NoSubjectParameterException, MissingPermissionException, InvalidProhibitionSubjectTypeException {
        Node user = analyticsService.getSessionUser(session);

        //get the target node from matrix params
        MultivaluedMap<String, String> targetParams = targetPs.getMatrixParameters();
        System.out.println(targetParams.getFirst("name") + " " + targetParams.getFirst("type") + " " + targetParams.getFirst("properties"));
        Node targetNode = nodeService.getNode(targetParams.getFirst("name"), targetParams.getFirst("type"), targetParams.getFirst("properties"), session, process);

        List<PmAnalyticsEntry> accessibleChildren = analyticsService.getAccessibleChildren(targetNode.getID(), user.getID());
        if(permissions != null && permissions.isEmpty()) {
            String[] permArr = permissions.split(",\\s*");

            accessibleChildren.removeIf(entry -> entry.getOperations().containsAll(Arrays.asList(permArr)));
        }

        return new ApiResponse(accessibleChildren).toResponse();
    }

    @Path("/{var1:target}/users/permissions")
    @GET
    public Response getUsersWithPermissions(@PathParam("var1") PathSegment targetPs,
                                            @QueryParam("permissions") String permissions,
                                            @QueryParam("session") String session,
                                            @QueryParam("process") long process) throws InvalidNodeTypeException, InvalidPropertyException, UnexpectedNumberOfNodesException, NodeNotFoundException, ConfigurationException, ClassNotFoundException, SQLException, DatabaseException, IOException, NoSubjectParameterException, SessionDoesNotExistException, InvalidProhibitionSubjectTypeException, SessionUserNotFoundException, MissingPermissionException {
        //get the target node from matrix params
        MultivaluedMap<String, String> targetParams = targetPs.getMatrixParameters();
        Node targetNode = nodeService.getNode(targetParams.getFirst("name"), targetParams.getFirst("type"), targetParams.getFirst("properties"), session, process);

        //Get all users' permissions on target
        List<PmAnalyticsEntry> usersPerms = analyticsService.getUsersPermissionsOn(targetNode.getID());

        //if there are permissions to check for, split the string and check
        if(permissions != null && permissions.length() > 0) {
            String[] permArr = permissions.split(",\\s*");

            List<PmAnalyticsEntry> usersWithPerms = new ArrayList<>();
            for(PmAnalyticsEntry entry : usersPerms) {
                if(entry.getOperations().containsAll(Arrays.asList(permArr))) {
                    usersWithPerms.add(entry);
                }
            }
;
            return new ApiResponse(usersWithPerms).toResponse();
        }

        return new ApiResponse(usersPerms).toResponse();
    }

    @Path("/{var1:target}/users/{username}/permissions")
    @GET
    public Response getUserPermissions(@PathParam("var1") PathSegment targetPs,
                                       @PathParam("username") String username,
                                       @QueryParam("session") String session,
                                       @QueryParam("process") long process) throws InvalidNodeTypeException, InvalidPropertyException, UnexpectedNumberOfNodesException, NodeNotFoundException, ConfigurationException, InvalidProhibitionSubjectTypeException, NoSubjectParameterException, ClassNotFoundException, SQLException, DatabaseException, IOException, SessionUserNotFoundException, MissingPermissionException, SessionDoesNotExistException {
        //get the target node from matrix params
        MultivaluedMap<String, String> targetParams = targetPs.getMatrixParameters();
        Node targetNode = nodeService.getNode(targetParams.getFirst("name"), targetParams.getFirst("type"), targetParams.getFirst("properties"), session, process);

        //get the user node
        Node userNode = nodeService.getNode(username, NodeType.USER.toString(), null, session, process);

        return new ApiResponse(analyticsService.getUserPermissionsOn(targetNode.getID(), userNode.getID()).getOperations()).toResponse();
    }

    @Path("/{var1:target}/users/{username}")
    @GET
    public Response checkUserHasPermissions(@PathParam("var1") PathSegment targetPs,
                                            @PathParam("username") String username,
                                            @QueryParam("permissions") String permissions,
                                            @QueryParam("session") String session,
                                            @QueryParam("process") long process) throws InvalidNodeTypeException, InvalidPropertyException, UnexpectedNumberOfNodesException, NodeNotFoundException, ConfigurationException, InvalidProhibitionSubjectTypeException, NoSubjectParameterException, ClassNotFoundException, SQLException, DatabaseException, IOException, SessionUserNotFoundException, MissingPermissionException, SessionDoesNotExistException {
        //get the target node from matrix params
        MultivaluedMap<String, String> targetParams = targetPs.getMatrixParameters();
        Node targetNode = nodeService.getNode(targetParams.getFirst("name"), targetParams.getFirst("type"), targetParams.getFirst("properties"), session, process);

        //get the user node
        Node userNode = nodeService.getNode(username, NodeType.USER.toString(), null, session, process);

        //get user permissions
        PmAnalyticsEntry userPerms = analyticsService.getUserPermissionsOn(targetNode.getID(), userNode.getID());

        //get permissions to check, if empty check if the user has any permissions
        if(permissions != null) {
            String[] permArr = permissions.split(",\\s*");
            return new ApiResponse(userPerms.getOperations().containsAll(Arrays.asList(permArr))).toResponse();
        } else {
            return new ApiResponse(!userPerms.getOperations().isEmpty()).toResponse();
        }
    }

    @Path("/{username}/targets/permissions")
    @GET
    public Response getAccessibleNodes(@PathParam("username") String username,
                                       @QueryParam("permissions") String permissions,
                                       @QueryParam("session") String session,
                                       @QueryParam("process") long process) throws InvalidNodeTypeException, InvalidPropertyException, UnexpectedNumberOfNodesException, NodeNotFoundException, NoUserParameterException, ConfigurationException, ClassNotFoundException, SQLException, IOException, DatabaseException, NoSubjectParameterException, SessionDoesNotExistException, InvalidProhibitionSubjectTypeException, SessionUserNotFoundException, MissingPermissionException {
        //get user node
        Node userNode = nodeService.getNode(username, NodeType.USER.toString(), null, session, process);

        //get all accessible nodes
        List<PmAnalyticsEntry> accessibleNodes = analyticsService.getAccessibleNodes(userNode.getID());

        if(permissions != null) {
            String[] permArr = permissions.split(",\\s*");
            List<PmAnalyticsEntry> entries = new ArrayList<>();
            for(PmAnalyticsEntry entry : accessibleNodes) {
                if(entry.getOperations().containsAll(Arrays.asList(permArr))) {
                    entries.add(entry);
                }
            }

            return new ApiResponse(entries).toResponse();
        } else {
            return new ApiResponse(accessibleNodes).toResponse();
        }
    }

    @Path("/sessions")
    @GET
    public Response getAccessibleNodes(@QueryParam("session") String session,
                                       @QueryParam("process") long process) throws NodeNotFoundException, NoUserParameterException, SessionUserNotFoundException, ConfigurationException, SessionDoesNotExistException, ClassNotFoundException, SQLException, IOException, DatabaseException, InvalidPropertyException {

        Node user = analyticsService.getSessionUser(session);
        return new ApiResponse(analyticsService.getAccessibleNodes(user.getID())).toResponse();
    }

    @GET
    @Path("/pos")
    public Response getPos(@QueryParam("session") String session,
                           @QueryParam("process") long process) throws SessionDoesNotExistException, IOException, SQLException, InvalidPropertyException, SessionUserNotFoundException, DatabaseException, ClassNotFoundException {
        return new ApiResponse(analyticsService.getPos(session)).toResponse();
    }
}
