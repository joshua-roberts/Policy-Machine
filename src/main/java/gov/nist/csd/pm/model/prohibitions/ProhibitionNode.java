package gov.nist.csd.pm.model.prohibitions;

import java.io.Serializable;

/**
 * The node that a prohibition is applied to.  The ID points to a real node in the NGAC graph, and the boolean complement
 * value determines whether to take the complement of this node when processing a prohibition.
 */
public class ProhibitionNode implements Serializable {
    long    id;
    boolean complement;

    public ProhibitionNode(){}

    public ProhibitionNode(long resourceID, boolean complement) {
        this.id = resourceID;
        this.complement = complement;
    }

    public long getID() {
        return id;
    }

    public boolean isComplement() {
        return complement;
    }

    public int hashCode(){
        return (int) id;
    }

    public boolean equals(Object o){
        if(o instanceof ProhibitionNode){
            ProhibitionNode n = (ProhibitionNode) o;
            return this.id == n.id;
        }
        return false;
    }
}
