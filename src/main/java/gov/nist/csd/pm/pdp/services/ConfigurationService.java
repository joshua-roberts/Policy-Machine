package gov.nist.csd.pm.pdp.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;
import gov.nist.csd.pm.common.model.prohibitions.Prohibition;
import gov.nist.csd.pm.pep.requests.CreateNodeRequest;


import javax.xml.soap.Node;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static gov.nist.csd.pm.common.constants.Properties.HASH_LENGTH;
import static gov.nist.csd.pm.common.constants.Properties.PASSWORD_PROPERTY;
import static gov.nist.csd.pm.common.model.graph.nodes.NodeType.UA;
import static gov.nist.csd.pm.common.model.graph.nodes.NodeUtils.generatePasswordHash;
import static gov.nist.csd.pm.pap.PAP.getPAP;

public class ConfigurationService extends Service {

    public ConfigurationService() {}

    // TODO add prohibition and obligations to json
    /**
     * Return a json string representation of the entire graph.  The json object will have 3 fields: nodes, assignments,
     * and associations.
     * @return The json string representation of the graph.
     * @throws PMException
     */
    public String save() throws PMException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        HashSet<NodeContext> nodes = getGraphPAP().getNodes();
        HashSet<JsonAssignment> jsonAssignments = new HashSet<>();
        HashSet<JsonAssociation> jsonAssociations = new HashSet<>();
        for(NodeContext node : nodes) {
            HashSet<NodeContext> parents = getGraphPAP().getParents(node.getID());

            for (NodeContext parent : parents) {
                jsonAssignments.add(new JsonAssignment(node.getID(), parent.getID()));
            }

            HashMap<Long, HashSet<String>> associations = getGraphPAP().getSourceAssociations(node.getID());
            for (long targetID : associations.keySet()) {
                HashSet<String> ops = associations.get(targetID);
                NodeContext targetNode = getGraphPAP().getNode(targetID);

                jsonAssociations.add(new JsonAssociation(node.getID(), targetNode.getID(), ops));
            }
        }

        return gson.toJson(new JsonGraph(nodes, jsonAssignments, jsonAssociations));
    }

    /**
     * Given a json string, presumably returned from calling save(), load the nodes, assignments, and associations into
     * the Policy Machine.  This does not erase any existing data.  The data loaded through this method will initially be
     * sent to only the database.  After processing the entire json structure, the in-memory graph is then also updated
     * with the information in the database.
     * @param config The json string containing the nodes, assignments, and associations
     * @throws PMException If the configuration is malformed or if the contents of the configuration are not consistent.
     */
    public void load(String config) throws PMException {
        JsonGraph graph = new Gson().fromJson(config, JsonGraph.class);

        HashSet<NodeContext> nodes = graph.getNodes();
        HashMap<Long, NodeContext> nodesMap = new HashMap<>();
        for(NodeContext node : nodes) {
            Map<String, String> properties = node.getProperties();

            //if a password is present encrypt it.
            // A password dis considered not encrypted if the length is less than 163 (HASH_LENGTH)
            if (properties != null &&
                    properties.get(PASSWORD_PROPERTY) != null &&
                    properties.get(PASSWORD_PROPERTY).length() < HASH_LENGTH) {
                try {
                    properties.put(PASSWORD_PROPERTY, generatePasswordHash(properties.get(PASSWORD_PROPERTY)));
                }
                catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                    throw new PMException(Errors.ERR_HASHING_USER_PSWD, e.getMessage());
                }
            }

            long newNodeID = getGraphPAP().createNode(node);
            nodesMap.put(node.getID(), node.id(newNodeID));
        }

        HashSet<JsonAssignment> assignments = graph.getAssignments();
        for(JsonAssignment assignment : assignments) {
            NodeContext childCtx = nodesMap.get(assignment.getChild());
            NodeContext parentCtx = nodesMap.get(assignment.getParent());
            System.out.println("{" +
                    assignment.getChild() + "=" + childCtx.getName() + ":" + childCtx.getType() +
                    assignment.getParent() + "=" + parentCtx.getName() + ":" + parentCtx.getType() +
                    "}");
            getGraphPAP().assign(childCtx, parentCtx);
        }

        HashSet<JsonAssociation> associations = graph.getAssociations();
        for(JsonAssociation association : associations) {
            System.out.println(association.getUa() + "-->" + association.getTarget() + association.getOps());
            long uaID = association.getUa();
            long targetID = association.getTarget();
            NodeContext targetNode = nodesMap.get(targetID);
            getGraphPAP().associate(new NodeContext(nodesMap.get(uaID).getID(), UA), new NodeContext(targetNode.getID(), targetNode.getType()), association.getOps());
        }
    }

    /**
     * Delete all nodes from the database.  Reinitialize the PAP to update the in-memory graph and recreate the super nodes.
     * @throws PMException
     */
    public void reset() throws PMException {
        HashSet<NodeContext> nodes = getGraphPAP().getNodes();
        for(NodeContext node : nodes) {
            getGraphPAP().deleteNode(node.getID());
        }

        //reinitialize the PAP to update the in memory graph
        getPAP().reinitialize();
    }

    class JsonAssignment {
        long child;
        long parent;

        public JsonAssignment(long child, long parent) {
            this.child = child;
            this.parent = parent;
        }

        public long getChild() {
            return child;
        }

        public long getParent() {
            return parent;
        }
    }

    class JsonAssociation {
        long            ua;
        long            target;
        HashSet<String> ops;

        public JsonAssociation(long ua, long target, HashSet<String> ops) {
            this.ua = ua;
            this.target = target;
            this.ops = ops;
        }

        public long getUa() {
            return ua;
        }

        public long getTarget() {
            return target;
        }

        public HashSet<String> getOps() {
            return ops;
        }
    }

    class JsonGraph {
        HashSet<NodeContext>     nodes;
        HashSet<JsonAssignment>  assignments;
        HashSet<JsonAssociation> associations;

        public JsonGraph(HashSet<NodeContext> nodes, HashSet<JsonAssignment> assignments, HashSet<JsonAssociation> associations) {
            this.nodes = nodes;
            this.assignments = assignments;
            this.associations = associations;
        }

        public HashSet<NodeContext> getNodes() {
            return nodes;
        }

        public HashSet<JsonAssignment> getAssignments() {
            return assignments;
        }

        public HashSet<JsonAssociation> getAssociations() {
            return associations;
        }
    }
}
