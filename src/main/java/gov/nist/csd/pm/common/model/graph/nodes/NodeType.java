package gov.nist.csd.pm.common.model.graph.nodes;

import java.io.Serializable;

/**
 * Allowed types of nodes in an NGAC Graph
 *
 * OA = Object Attribute
 * UA = User Attribute
 * U = User
 * O = Object
 * PC = Policy Class
 * OS = Operation Set
 */
public enum NodeType implements Serializable {
    OA("OA"),
    UA("UA"),
    U("U"),
    O("O"),
    PC("PC"),
    OS("OS");

    private String label;
    NodeType(String label){
        this.label = label;
    }
    public String toString(){
        return label;
    }

    /**
     * Given a string, return the matching NodeType. If the type is null or not one of the types listed above,
     * an InvalidNodeTypeExceptino will be thrown.
     * @param type The String type to convert to a NodeType.
     * @return The equivalent NodeType of the given String.
     * @throws IllegalArgumentException When an invalid or null type is provided.
     */
    public static NodeType toNodeType(String type) {
        if(type == null){
            throw new IllegalArgumentException("null is not a valid NGAC NodeType");
        }
        switch (type.toUpperCase()){
            case "OA":
                return NodeType.OA;
            case "UA":
                return NodeType.UA;
            case "U":
                return NodeType.U;
            case "O":
                return NodeType.O;
            case "PC":
                return NodeType.PC;
            case "OS":
                return NodeType.OS;
            default:
                throw new IllegalArgumentException(type + " is not a valid NGAC NodeType");
        }
    }
}