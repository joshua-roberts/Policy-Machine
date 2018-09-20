package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.model.exceptions.ConfigurationException;
import gov.nist.csd.pm.model.exceptions.DatabaseException;
import gov.nist.csd.pm.pdp.services.AssignmentService;
import gov.nist.csd.pm.pdp.services.AssociationsService;
import gov.nist.csd.pm.pdp.services.NodeService;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.SQLException;

@Path("/nlpm")
public class NLPMResource {

    NodeService         nodeService        = new NodeService();
    AssignmentService   assignmentService  = new AssignmentService();
    AssociationsService associationsService = new AssociationsService();

    public NLPMResource() throws ConfigurationException, DatabaseException, IOException, ClassNotFoundException, SQLException {
    }

    @POST
    public Response nlpm(String[] statements) {
        for(String stmt : statements) {
            System.out.println(stmt);
        }

        return null;
    }



    /*@POST
    public Response grant(GrantRequest request) throws PmException, IOException, ClassNotFoundException, SQLException {
        //get the pc node
        Node pcNode = nodeService.getNode(request.getPcID());


        //get the subjectNode
        Node subjectNode = nodeService.getNode(request.getSubjectID());

        //generate a random name to give generated UA and OA
        String genName = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();

        //if subject is a user, create a new UA
        if(subjectNode.getType().equals(NodeType.U)) {
            Node uaNode = nodeService.createNode(0, NEW_NODE_ID, genName, NodeType.UA.toString(),
                    new Property[]{
                            new Property(DESCRIPTION_PROPERTY, "Generated User Attribute for " + subjectNode.getName())
                    });

            //assign user to UA
            assignmentService.createAssignment(subjectNode.getID(), uaNode.getID());

            //set uaNode as subjectNnode
            subjectNode = uaNode;
        }


        //create new Container node to hold all targets
        Node oaNode = nodeService.createNode(0, NEW_NODE_ID, genName, NodeType.OA.toString(),
                new Property[]{
                        new Property(DESCRIPTION_PROPERTY, "Generated Object Attribute ")
                });

        //assign all target nodes to oaNode
        for(long target : request.getTargetIDs()) {
            assignmentService.createAssignment(target, oaNode.getID());
        }

        //assign OA to PC
        assignmentService.createAssignment(oaNode.getID(), pcNode.getID());

        //assign UA to PC
        assignmentService.createAssignment(subjectNode.getID(), pcNode.getID());

        //create association between subject/UA and Container
        associationsService.createAssociation(subjectNode.getID(), oaNode.getID(), new HashSet<>(Arrays.asList(request.getOperations())), true);

        return new ApiResponse("Success!").toResponse();
    }*/


}
