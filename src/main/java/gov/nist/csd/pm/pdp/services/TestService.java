package gov.nist.csd.pm.pdp.services;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.OldNode;
import gov.nist.csd.pm.model.graph.nodes.NodeType;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.SQLException;
import java.util.*;

public class TestService extends Service {

    public Map<String, Object> getTestGraph(String session, long process) throws ClassNotFoundException, SQLException, InvalidPropertyException, IOException, DatabaseException, InvalidAssignmentException, NoSubjectParameterException, NoSuchAlgorithmException, ConfigurationException, InvalidNodeTypeException, SessionDoesNotExistException, InvalidProhibitionSubjectTypeException, MissingPermissionException, UnexpectedNumberOfNodesException, NullNameException, NullTypeException, NodeNameExistsException, NodeIDExistsException, PropertyNotFoundException, InvalidAssociationException, InvalidKeySpecException, SessionUserNotFoundException, NodeNotFoundException, AssignmentExistsException, AssociationExistsException, NodeNameExistsInNamespaceException, PolicyClassNameExistsException {
        //create nodes
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        Map<String, String> properties = new HashMap<>();
        properties.put("namespace", uuid);

        NodeService nodeService = new NodeService();
        OldNode pc1 = nodeService.createPolicy("pc1", properties, session, process);
        OldNode oa1 = nodeService.createNodeIn(pc1.getID(), "oa1", NodeType.OA.toString(), properties, session, process);
        OldNode ua1 = nodeService.createNodeIn(pc1.getID(), "ua1", NodeType.UA.toString(), properties, session, process);
        OldNode u1 = nodeService.createNodeIn(ua1.getID(), "u1", NodeType.U.toString(), properties, session, process);
        OldNode o1 = nodeService.createNodeIn(oa1.getID(), "o1", NodeType.O.toString(), properties, session, process);

        Map<String, Object> map = new HashMap<>();
        map.put("uuid", uuid);
        map.put("oID", o1.getID());
        map.put("uID", u1.getID());
        map.put("uaID", ua1.getID());
        map.put("oaID", oa1.getID());
        map.put("pcID", pc1.getID());

        return map;
    }

    public void deleteTestGraph(String uuid) throws SQLException, IOException, ClassNotFoundException, InvalidPropertyException, DatabaseException, InvalidNodeTypeException, NodeNotFoundException {
        Map<String, String> properties = new HashMap<>();
        properties.put("namespace", uuid);

        NodeService nodeService = new NodeService();
        Set<OldNode> nodes = nodeService.getNodes(null, null, properties);
        for (OldNode node : nodes) {
            getDaoManager().getNodesDAO().deleteNode(node.getID());
            getGraph().deleteNode(node);

            if(node.getType().equals(NodeType.PC)) {
                // delete pc admin and pc oa
                Set<OldNode> subNodes = nodeService.getNodes(node.getName(), NodeType.OA.toString(), properties);
                for(OldNode pcOA : subNodes) {
                    nodeService.deleteNode(pcOA.getID());
                }
                subNodes = nodeService.getNodes(node.getName() + " admin", NodeType.OA.toString(), properties);
                for(OldNode pcOA : subNodes) {
                    nodeService.deleteNode(pcOA.getID());
                }

            }
        }
    }
}
