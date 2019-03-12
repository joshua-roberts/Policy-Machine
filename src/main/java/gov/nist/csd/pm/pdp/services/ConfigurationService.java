package gov.nist.csd.pm.pdp.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nist.csd.pm.common.exceptions.*;
import gov.nist.csd.pm.exceptions.PMException;
import gov.nist.csd.pm.graph.model.nodes.Node;
import gov.nist.csd.pm.pap.PAP;
import gov.nist.csd.pm.prohibitions.model.Prohibition;


import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static gov.nist.csd.pm.common.constants.Properties.HASH_LENGTH;
import static gov.nist.csd.pm.common.constants.Properties.PASSWORD_PROPERTY;
import static gov.nist.csd.pm.common.util.NodeUtils.generatePasswordHash;
import static gov.nist.csd.pm.graph.model.nodes.NodeType.UA;
import static gov.nist.csd.pm.pap.PAP.getPAP;

public class ConfigurationService extends Service {

    public ConfigurationService() {}

    /**
     * Return a json string representation of the entire graph.  The json object will have 3 fields: nodes, assignments,
     * and associations.
     * @return the json string representation of the graph.
     * @throws PMException If there is an error reading the graph information from the database and saving it to json.
     */
    public String save() throws PMException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Collection<Node> nodes = getGraphPAP().getNodes();
        Set<JsonAssignment> jsonAssignments = new HashSet<>();
        Set<JsonAssociation> jsonAssociations = new HashSet<>();
        for(Node node : nodes) {
            Set<Long> parents = getGraphPAP().getParents(node.getID());

            for (Long parent : parents) {
                jsonAssignments.add(new JsonAssignment(node.getID(), parent));
            }

            Map<Long, Set<String>> associations = getGraphPAP().getSourceAssociations(node.getID());
            for (long targetID : associations.keySet()) {
                Set<String> ops = associations.get(targetID);
                Node targetNode = getGraphPAP().getNode(targetID);

                jsonAssociations.add(new JsonAssociation(node.getID(), targetNode.getID(), ops));
            }
        }

        List<Prohibition> prohibitions = getProhibitionsPAP().getProhibitions();

        return gson.toJson(new Configuration(new JsonGraph(nodes, jsonAssignments, jsonAssociations), prohibitions));
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
        Configuration configuration = new Gson().fromJson(config, Configuration.class);

        JsonGraph graph = configuration.getGraph();
        Collection<Node> nodes = graph.getNodes();
        HashMap<Long, Node> nodesMap = new HashMap<>();
        for(Node node : nodes) {
            long oldID = node.getID();
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
                    throw new PMGraphException(e.getMessage());
                }
            }

            getGraphPAP().createNode(node);
            nodesMap.put(oldID, node);
        }

        Set<JsonAssignment> assignments = graph.getAssignments();
        for(JsonAssignment assignment : assignments) {
            Node childCtx = nodesMap.get(assignment.getChild());
            Node parentCtx = nodesMap.get(assignment.getParent());
            getGraphPAP().assign(childCtx, parentCtx);
        }

        Set<JsonAssociation> associations = graph.getAssociations();
        for(JsonAssociation association : associations) {
            long uaID = association.getUa();
            long targetID = association.getTarget();
            Node targetNode = nodesMap.get(targetID);
            getGraphPAP().associate(new Node(nodesMap.get(uaID).getID(), UA), new Node(targetNode.getID(), targetNode.getType()), association.getOps());
        }

        // prohibitions
        List<Prohibition> prohibitions = configuration.getProhibitions();
        for(Prohibition prohibition : prohibitions) {
            getProhibitionsPAP().createProhibition(prohibition);
        }
    }

    /**
     * Delete all nodes from the database.  Reinitialize the PAP to update the in-memory graph and recreate the super nodes.
     *
     * @throws PMException If there is an error resetting the graph or reinitializing the PAP.
     */
    public void reset() throws PMException {
        // reset the pap
        getPAP().reset();
    }

    public void loadSuper() throws PMException {
        getPAP().loadSuper();
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
        Set<String> ops;

        public JsonAssociation(long ua, long target, Set<String> ops) {
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

        public Set<String> getOps() {
            return ops;
        }
    }

    class Configuration {
        private JsonGraph         graph;
        private List<Prohibition> prohibitions;

        public Configuration(JsonGraph graph, List<Prohibition> prohibitions) {
            this.graph = graph;
            this.prohibitions = prohibitions;
        }

        public JsonGraph getGraph() {
            return graph;
        }

        public void setGraph(JsonGraph graph) {
            this.graph = graph;
        }

        public List<Prohibition> getProhibitions() {
            return prohibitions;
        }

        public void setProhibitions(List<Prohibition> prohibitions) {
            this.prohibitions = prohibitions;
        }
    }

    class JsonGraph {
        Collection<Node>     nodes;
        Set<JsonAssignment>  assignments;
        Set<JsonAssociation> associations;

        public JsonGraph(Collection<Node> nodes, Set<JsonAssignment> assignments, Set<JsonAssociation> associations) {
            this.nodes = nodes;
            this.assignments = assignments;
            this.associations = associations;
        }

        public Collection<Node> getNodes() {
            return nodes;
        }

        public Set<JsonAssignment> getAssignments() {
            return assignments;
        }

        public Set<JsonAssociation> getAssociations() {
            return associations;
        }
    }
}
