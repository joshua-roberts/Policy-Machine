package gov.nist.csd.pm.pdp.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.exceptions.LoaderException;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static gov.nist.csd.pm.model.constants.Properties.HASH_LENGTH;
import static gov.nist.csd.pm.model.constants.Properties.PASSWORD_PROPERTY;
import static gov.nist.csd.pm.pap.PAP.getPAP;

public class ConfigurationService extends Service {

    public ConfigurationService(String sessionID, long processID) {
        super(sessionID, processID);
    }

    public ConfigurationService() {
        super("CONFIG_SESSION_ID", 0);
    }

    public String save() throws LoadConfigException, DatabaseException, LoaderException, MissingPermissionException, NodeNotFoundException, SessionDoesNotExistException, InvalidNodeTypeException, InvalidProhibitionSubjectTypeException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        HashSet<Node> nodes = getGraphDB().getNodes();
        HashSet<JsonAssignment> jsonAssignments = new HashSet<>();
        HashSet<JsonAssociation> jsonAssociations = new HashSet<>();
        for(Node node : nodes) {
            HashSet<Node> parents = getGraphDB().getParents(node.getID());

            for (Node parent : parents) {
                jsonAssignments.add(new JsonAssignment(node.getID(), parent.getID()));
            }

            HashMap<Long, HashSet<String>> associations = getGraphDB().getSourceAssociations(node.getID());
            for (long targetID : associations.keySet()) {
                HashSet<String> ops = associations.get(targetID);
                Node targetNode = getSearch().getNode(targetID);

                jsonAssociations.add(new JsonAssociation(node.getID(), targetNode.getID(), ops));
            }
        }

        return gson.toJson(new JsonGraph(nodes, jsonAssignments, jsonAssociations));
    }

    public void load(String config) throws HashingUserPasswordException, LoadConfigException, DatabaseException, LoaderException, NullNodeException, NoIDException, NullTypeException, NullNameException, NodeNotFoundException, SessionDoesNotExistException, InvalidAssignmentException, MissingPermissionException, InvalidNodeTypeException, InvalidAssociationException, InvalidProhibitionSubjectTypeException {
        JsonGraph graph = new Gson().fromJson(config, JsonGraph.class);

        HashSet<Node> nodes = graph.getNodes();
        HashMap<Long, Node> nodesMap = new HashMap<>();
        for(Node node : nodes) {
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
                    throw new HashingUserPasswordException();
                }
            }

            long newNodeID = getGraphDB().createNode(node);
            nodesMap.put(node.getID(), node.id(newNodeID));
        }

        HashSet<JsonAssignment> assignments = graph.getAssignments();
        for(JsonAssignment assignment : assignments) {
            Node childCtx = nodesMap.get(assignment.getChild());
            Node parentCtx = nodesMap.get(assignment.getParent());
            System.out.println("{" +
                    assignment.getChild() + "=" + childCtx.getName() + ":" + childCtx.getType() +
                    assignment.getParent() + "=" + parentCtx.getName() + ":" + parentCtx.getType() +
                    "}");
            getGraphDB().assign(childCtx.getID(), childCtx.getType(), parentCtx.getID(), parentCtx.getType());
        }

        HashSet<JsonAssociation> associations = graph.getAssociations();
        for(JsonAssociation association : associations) {
            System.out.println(association.getUa() + "-->" + association.getTarget() + association.getOps());
            long uaID = association.getUa();
            long targetID = association.getTarget();
            Node targetNode = nodesMap.get(targetID);
            getGraphDB().associate(nodesMap.get(uaID).getID(), targetNode.getID(), targetNode.getType(), association.getOps());
        }
    }

    public void reset() throws DatabaseException, LoadConfigException, LoaderException, MissingPermissionException, NodeNotFoundException, SessionDoesNotExistException, InvalidProhibitionSubjectTypeException {
        HashSet<Node> nodes = getGraphMem().getNodes();
        for(Node node : nodes) {
            getGraphDB().deleteNode(node.getID());
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
        HashSet<Node>            nodes;
        HashSet<JsonAssignment>  assignments;
        HashSet<JsonAssociation> associations;

        public JsonGraph(HashSet<Node> nodes, HashSet<JsonAssignment> assignments, HashSet<JsonAssociation> associations) {
            this.nodes = nodes;
            this.assignments = assignments;
            this.associations = associations;
        }

        public HashSet<Node> getNodes() {
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
