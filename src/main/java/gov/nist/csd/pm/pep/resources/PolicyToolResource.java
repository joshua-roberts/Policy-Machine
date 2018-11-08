package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.Graph;
import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.graph.nodes.NodeType;
import gov.nist.csd.pm.pap.graph.MemGraph;
import gov.nist.csd.pm.pap.loader.graph.DummyGraphLoader;
import gov.nist.csd.pm.model.exceptions.LoaderException;
import gov.nist.csd.pm.pdp.engine.MemPolicyDecider;
import gov.nist.csd.pm.pdp.engine.PolicyDecider;
import gov.nist.csd.pm.pep.requests.SandboxRequest;
import gov.nist.csd.pm.pep.response.ApiResponse;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static gov.nist.csd.pm.model.Constants.COMMA_DELIMETER;
import static gov.nist.csd.pm.model.constants.Properties.PASSWORD_PROPERTY;
import static gov.nist.csd.pm.model.graph.nodes.Node.hashID;
import static gov.nist.csd.pm.pap.PAP.getPAP;


@Path("/sandbox")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PolicyToolResource {

    private static HashMap<String, Graph>               graphs     = new HashMap<>();
    private static HashMap<String, HashMap<Long, Node>> graphNodes = new HashMap<>();

    @POST
    public Response createSandbox() throws LoaderException {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        graphs.put(uuid, new MemGraph(new DummyGraphLoader()));
        return ApiResponse.Builder.success().entity(uuid).build();
    }

    @Path("/{graphID}")
    @POST
    public Response buildSandbox(@PathParam("graphID") String graphID, SandboxRequest request) throws IOException, SAXException, ParserConfigurationException, NullNameException, LoadConfigException, NullTypeException, NullNodeException, LoaderException, DatabaseException, NoIDException, InvalidNodeTypeException, NodeNotFoundException, SessionDoesNotExistException, InvalidAssignmentException, MissingPermissionException, InvalidAssociationException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidProhibitionSubjectTypeException {
        Graph graph = new MemGraph(new DummyGraphLoader());

        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(request.getSource()));

        Document doc = db.parse(is);
        doc.getDocumentElement().normalize();
        Element root = doc.getDocumentElement();

        List<org.w3c.dom.Node> childNodes = getChildNodes(root);
        for(org.w3c.dom.Node pc : childNodes) {
            parseNode(graphID, graph, pc);
        }

        graphs.put(graphID, graph);

        return ApiResponse.Builder.success().build();
    }

    private Node parseNode(String graphID, Graph g, org.w3c.dom.Node n) throws NullNameException, LoadConfigException, DatabaseException, NullTypeException, NullNodeException, LoaderException, NoIDException, InvalidNodeTypeException, NodeNotFoundException, SessionDoesNotExistException, InvalidAssignmentException, MissingPermissionException, InvalidAssociationException, InvalidKeySpecException, NoSuchAlgorithmException, InvalidProhibitionSubjectTypeException {
        if(n.getNodeName().equals("a")) {

            NamedNodeMap attributes = n.getAttributes();
            org.w3c.dom.Node sourceNode = attributes.getNamedItem("source");
            long source = hashID(sourceNode.getNodeValue(), NodeType.UA, graphID);

            org.w3c.dom.Node oaNode = attributes.getNamedItem("oa");
            org.w3c.dom.Node uaNode = attributes.getNamedItem("ua");
            String name;
            NodeType type;
            if(oaNode != null) {
                name = oaNode.getNodeValue();
                type = NodeType.OA;
            } else {
                name = uaNode.getNodeValue();
                type = NodeType.UA;
            }
            long target = hashID(name, type, graphID);

            org.w3c.dom.Node opNode = attributes.getNamedItem("ops");
            String[] pieces = opNode.getNodeValue().split(COMMA_DELIMETER);
            HashSet<String> ops = new HashSet<>(Arrays.asList(pieces));

            //create the association
            g.associate(source, target, type, ops);
            return null;
        } else {
            //create the node
            NamedNodeMap attrs = n.getAttributes();
            org.w3c.dom.Node name = attrs.getNamedItem("name");
            NodeType nt = NodeType.toNodeType(n.getNodeName().toUpperCase());
            HashMap<String, String> properties = new HashMap<>();
            properties.put("namespace", graphID);
            for(int i = 0; i < attrs.getLength(); i++) {
                if (attrs.item(i).getNodeName().equals("name") || attrs.item(i).getNodeName().equals("type")) {
                    continue;
                }
                properties.put(attrs.item(i).getNodeName(), attrs.item(i).getNodeValue());
            }
            long id = hashID(name.getNodeValue(), nt, properties.get("namespace"));
            if(properties.get(PASSWORD_PROPERTY) != null) {
                String pass = properties.get(PASSWORD_PROPERTY);
                String passwordHash = generatePasswordHash(pass);
                properties.put(PASSWORD_PROPERTY, passwordHash);
            }
            Node node = new Node(id, name.getNodeValue(), nt, properties);
            if(!g.exists(id)) {
                //create the node in the graph
                g.createNode(node);

                //store node in map
                HashMap<Long, Node> nodes = graphNodes.get(graphID);
                if (nodes == null) {
                    nodes = new HashMap<>();
                }
                nodes.put(id, node);
                graphNodes.put(graphID, nodes);
            }

            //create any children
            NodeList childNodes = n.getChildNodes();
            for(int i = 0; i < childNodes.getLength(); i++) {
                if(!childNodes.item(i).getNodeName().startsWith("#")) {
                    Node childNode = parseNode(graphID, g, childNodes.item(i));
                    if(childNode != null) {
                        g.assign(childNode.getID(), childNode.getType(), node.getID(), node.getType());
                    }
                }
            }

            return node;
        }
    }

    private List<org.w3c.dom.Node> getChildNodes(org.w3c.dom.Node node) {
        List<org.w3c.dom.Node> nodes = new ArrayList<>();
        NodeList childNodes = node.getChildNodes();
        for(int i = 0; i < childNodes.getLength(); i++) {
            if(!childNodes.item(i).getNodeName().startsWith("#")) {
                nodes.add(childNodes.item(i));
            }
        }

        return nodes;
    }

    /**
     * Utility method to hash a password.
     * @param password The plaintext password to hash.
     * @return The hash of the password;
     */
    static String generatePasswordHash(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        int iterations = 100;
        char[] chars = password.toCharArray();
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
        byte[] salt = new byte[16];
        sr.nextBytes(salt);

        PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 64 * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return iterations + toHex(salt) + toHex(hash);
    }

    /**
     * Utility method to check that a plain text password matches a hashed password.
     * @param stored The hash of the password.
     * @param toCheck The plaintext password to check against the hashed.
     * @return True if the passwords match, false otherwise.
     */
    static boolean checkPasswordHash(String stored, String toCheck) throws NoSuchAlgorithmException, InvalidKeySpecException{
        String part0 = stored.substring(0, 3);
        String part1 = stored.substring(3, 35);
        String part2 = stored.substring(35);
        int iterations = Integer.parseInt(part0);
        byte[] salt = fromHex(part1);
        byte[] hash = fromHex(part2);

        PBEKeySpec spec = new PBEKeySpec(toCheck.toCharArray(), salt, iterations, hash.length * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] testHash = skf.generateSecret(spec).getEncoded();

        String x = toHex(testHash);

        int diff = hash.length ^ testHash.length;
        for(int i = 0; i < hash.length && i < testHash.length; i++)
        {
            diff |= hash[i] ^ testHash[i];
            if(hash[i] != testHash[i]){
                int cx = 0;
            }
        }
        return diff == 0;
    }

    private static byte[] fromHex(String hex)
    {
        byte[] bytes = new byte[hex.length() / 2];
        for(int i = 0; i<bytes.length ;i++)
        {
            bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        }
        return bytes;
    }

    private static String toHex(byte[] array)
    {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0)
        {
            return String.format("%0" + paddingLength + "d", 0) + hex;
        }else{
            return hex;
        }
    }

    @Path("/{graphID}")
    @PUT
    public Response commitSandbox(@PathParam("graphID") String graphID) throws LoaderException, DatabaseException, LoadConfigException, NullNodeException, NoIDException, NullTypeException, NullNameException, MissingPermissionException, NodeNotFoundException, SessionDoesNotExistException, InvalidAssignmentException, InvalidNodeTypeException, InvalidAssociationException, InvalidProhibitionSubjectTypeException {
        Graph graph = graphs.get(graphID);

        //create graph in db
        HashMap<Long, Node> nodes = graphNodes.get(graphID);
        for(Long id : nodes.keySet()) {
            Node n = nodes.get(id);
            //create node
            getPAP().getGraphDB().createNode(n);
        }

        for(long id : nodes.keySet()) {
            Node n = nodes.get(id);
            //create assignments
            HashSet<Node> parents = graph.getParents(n.getID());
            for(Node p : parents) {
                Node parentNode = nodes.get(p.getID());
                getPAP().getGraphDB().assign(n.getID(), n.getType(), parentNode.getID(), parentNode.getType());
            }
            //create associations
            HashMap<Long, HashSet<String>> assocs = graph.getSourceAssociations(n.getID());
            for(Long targetID : assocs.keySet()){
                Node node = nodes.get(targetID);
                getPAP().getGraphDB().associate(n.getID(), node.getID(), node.getType(), assocs.get(targetID));
            }
        }

        //reinitialize pap
        getPAP().reinitialize();

        //clear graph from memory
        graphNodes.remove(graphID);
        graphs.remove(graphID);

        return ApiResponse.Builder.success().build();
    }

    @Path("/{graphID}/permissions")
    @GET
    public Response getGraphPermissions(@PathParam("graphID") String graphID) throws DatabaseException, LoadConfigException, NodeNotFoundException, SessionDoesNotExistException, LoaderException, MissingPermissionException, InvalidProhibitionSubjectTypeException {
        Graph graph = graphs.get(graphID);
        HashMap<Long, Node> nodes = graphNodes.get(graphID);
        //get the users
        HashSet<Node> users = new HashSet<>();
        for(Long nodeID : nodes.keySet()) {
            Node node = nodes.get(nodeID);
            if(node.getType().equals(NodeType.U)) {
                users.add(node);
            }
        }
        //get the objects
        HashSet<Node> objects = new HashSet<>();
        for(Long nodeID : nodes.keySet()) {
            Node node = nodes.get(nodeID);
            if(node.getType().equals(NodeType.O) || node.getType().equals(NodeType.OA)) {
                objects.add(node);
            }
        }

        //for each user get the permissions it has on each object
        PolicyDecider decider = new MemPolicyDecider(graph, getPAP().getProhibitionsMem().getProhibitions());
        PermissionsResponse response = new PermissionsResponse();
        for(Node user : users) {
            PermissionsResponse.PermissionSet set = new PermissionsResponse.PermissionSet(user);
            for(Node object : objects) {
                HashSet<String> perms = decider.listPermissions(user.getID(), 0, object.getID());
                set.addPermissions(new PermissionsResponse.Permissions(object, perms));
            }
            response.addPermissionSet(set);
        }

        return ApiResponse.Builder.success().entity(response).build();
    }

    static class PermissionsResponse {
        List<PermissionSet> permissionSets;

        public PermissionsResponse() {
            permissionSets = new ArrayList<>();
        }

        public PermissionsResponse(List<PermissionSet> permissionSets) {
            this.permissionSets = permissionSets;
        }

        public List<PermissionSet> getPermissionSets() {
            return permissionSets;
        }

        public void setPermissionSets(List<PermissionSet> permissionSets) {
            this.permissionSets = permissionSets;
        }

        public void addPermissionSet(PermissionSet set) {
            this.permissionSets.add(set);
        }

        static class PermissionSet {
            Node user;
            List<Permissions> permissions;

            public PermissionSet(Node node) {
                user = node;
                permissions = new ArrayList<>();
            }

            public Node getUser() {
                return user;
            }

            public void setUser(Node user) {
                this.user = user;
            }

            public List<Permissions> getPermissions() {
                return permissions;
            }

            public void setPermissions(List<Permissions> permissions) {
                this.permissions = permissions;
            }

            public void addPermissions(Permissions permissions) {
                this.permissions.add(permissions);
            }
        }

        static class Permissions {
            Node target;
            HashSet<String> operations;

            public Permissions(Node target, HashSet<String> operations) {
                this.target = target;
                this.operations = operations;
            }

            public Node getTarget() {
                return target;
            }

            public void setTarget(Node target) {
                this.target = target;
            }

            public HashSet<String> getOperations() {
                return operations;
            }

            public void setOperations(HashSet<String> operations) {
                this.operations = operations;
            }
        }
    }

    @Path("/{graphID}/nodes/{nodeID}")
    @GET
    public Response getGraphNode(@PathParam("graphID") String graphID, @PathParam("nodeID") long nodeID) {
        return ApiResponse.Builder.success().entity(graphNodes.get(graphID).get(nodeID)).build();
    }
}
