package gov.nist.csd.pm.pep.resources;

import gov.nist.csd.pm.model.exceptions.*;
import gov.nist.csd.pm.model.graph.nodes.Node;
import gov.nist.csd.pm.model.graph.nodes.NodeType;
import gov.nist.csd.pm.pdp.PDP;
import gov.nist.csd.pm.pep.requests.CreateNodeRequest;
import gov.nist.csd.pm.pep.response.ApiResponse;
import gov.nist.csd.pm.pip.loader.LoaderException;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

import static gov.nist.csd.pm.model.constants.Properties.PASSWORD_PROPERTY;

@Path("/nodes")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NodeResource {

    @GET
    public Response getNodes(@Context UriInfo uriInfo,
                             @QueryParam("session") String session,
                             @QueryParam("process") long process) throws LoadConfigException, LoaderException, DatabaseException, SessionDoesNotExistException {

        MultivaluedMap<String, String> queryParameters = uriInfo.getQueryParameters();
        Map<String, String> properties = new HashMap<>();
        for (String key : queryParameters.keySet()) {
            if (key.equalsIgnoreCase("name") ||
                    key.equalsIgnoreCase("type") ||
                    key.equalsIgnoreCase("session")) {
                continue;
            }

            String value = queryParameters.getFirst(key);
            properties.put(key, value);
        }

        PDP pdp = new PDP(session, process);
        HashSet<Node> nodes = pdp.search(properties.get("name"), properties.get("type"), properties);

        return ApiResponse.Builder
                .success()
                .entity(nodes)
                .build();
    }

    @Path("/policies")
    @POST
    public Response createPolicy(CreateNodeRequest request,
                                 @QueryParam("session") String session,
                                 @QueryParam("process") long process) throws PMException {
        PDP pdp = new PDP(session, process);

        //get the request parameters
        String name = request.getName();
        HashMap<String, String> properties = request.getProperties();

        //check that the parameters are not null
        if(name == null) {
            throw new NullNameException();
        } else if (properties == null) {
            // if the properties are null, instantiate
            properties = new HashMap<>();
        }

        //check that the PC name does not exist
        Set<Node> nodes = pdp.search(name, NodeType.PC.toString(), null);
        if (!nodes.isEmpty()) {
            throw new PolicyClassNameExistsException(name);
        }

        //create the context for the policy class
        Node ctx = new Node(request.getName(), NodeType.PC, request.getProperties())
                .name(request.getName())
                .properties(properties);
        Node node = pdp.createPolicy(ctx);

        return ApiResponse.Builder
                .success()
                .entity(node)
                .build();
    }

    @Path("/{nodeID}")
    @GET
    public Response getNode(@PathParam("nodeID") long nodeID,
                            @QueryParam("content") boolean content,
                            @QueryParam("session") String session,
                            @QueryParam("process") long process) throws PMException {
        PDP pdp = new PDP(session, process);
        if(!pdp.exists(nodeID)) {
            throw new NodeNotFoundException(nodeID);
        }

        Node node = pdp.getNode(nodeID);
        return ApiResponse.Builder
                .success()
                .entity(node)
                .build();
    }

    @Path("/{nodeID}")
    @DELETE
    public Response deleteNode(@PathParam("nodeID") long id,
                               @QueryParam("session") String session,
                               @QueryParam("process") long process) throws PMException {
        PDP pdp = new PDP(session, process);
        pdp.deleteNode(id);
        return ApiResponse.Builder
                .success()
                .build();
    }

    @Path("{nodeID}/children")
    @POST
    public Response createNodeIn(@PathParam("nodeID") long nodeID,
                                 CreateNodeRequest request,
                                 @QueryParam("content") boolean content,
                                 @QueryParam("session") String session,
                                 @QueryParam("process") long process) throws PMException {
        PDP pdp = new PDP(session, process);

        //get the request parameters
        String name = request.getName();
        String type = request.getType();
        HashMap<String, String> properties = request.getProperties();

        //check that the parameters are not null
        if(name == null) {
            throw new NullNameException();
        }else if (type == null) {
            throw new NullTypeException();
        } else if (properties == null) {
            // if the properties are null, instantiate
            properties = new HashMap<>();
        }

        //check that the parent node exists
        if(!pdp.exists(nodeID)) {
            throw new NodeNotFoundException(nodeID);
        }

        //if this node is a user, hash the password if present in the properties
        if(properties.containsKey(PASSWORD_PROPERTY)) {
            try {
                properties.put(PASSWORD_PROPERTY, generatePasswordHash(properties.get(PASSWORD_PROPERTY)));
            }
            catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new HashingUserPasswordException();
            }
        }

        //create the node
        Node ctx = new Node(request.getName(), NodeType.toNodeType(type), properties);
        Node newNode = pdp.createNode(ctx);

        //get the parent node to ake the assignment
        Node parentNode = pdp.getNode(nodeID);

        //create node contexts for the new node and the parent node
        Node childCtx = new Node(newNode.getID(), newNode.getType());
        Node parentCtx = new Node(nodeID, parentNode.getType());
        //assign the new node to the parent
        pdp.assign(childCtx, parentCtx);

        return ApiResponse.Builder
                .success()
                .entity(newNode)
                .build();
    }


    @Path("{nodeID}/children")
    @GET
    public Response getNodeChildren(@PathParam("nodeID") long id,
                                    @QueryParam("type") String type,
                                    @QueryParam("session") String session,
                                    @QueryParam("process") long process) throws PMException {
        PDP pdp = new PDP(session, process);
        if(!pdp.exists(id)) {
            throw new NodeNotFoundException(id);
        }
        HashSet<Node> children = pdp.getChildren(id);
        return ApiResponse.Builder
                .success()
                .entity(children)
                .build();
    }

    @Path("/{nodeID}/parents")
    @GET
    public Response getNodeParents(@PathParam("nodeID") long id,
                                   @QueryParam("type") String type,
                                   @QueryParam("session") String session,
                                   @QueryParam("process") long process) throws PMException {
        PDP pdp = new PDP(session, process);
        if(!pdp.exists(id)) {
            throw new NodeNotFoundException(id);
        }
        HashSet<Node> parents = pdp.getParents(id);
        return ApiResponse.Builder
                .success()
                .entity(parents)
                .build();
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
}
