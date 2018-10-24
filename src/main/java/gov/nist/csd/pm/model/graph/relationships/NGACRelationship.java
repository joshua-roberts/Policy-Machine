package gov.nist.csd.pm.model.graph.relationships;

import org.jgrapht.graph.DefaultEdge;

import java.io.Serializable;

/**
 * This class will serve as a parent class for Assignments and Associations
 */
public class NGACRelationship<V> extends DefaultEdge implements Serializable {
    private long sourceID;
    private long targetID;

    public NGACRelationship() {

    }

    public NGACRelationship(long sourceID, long targetID) {
        this.sourceID = sourceID;
        this.targetID = targetID;
    }

    public long getSourceID() {
        return sourceID;
    }

    public void setSourceID(long sourceID) {
        this.sourceID = sourceID;
    }

    public long getTargetID() {
        return targetID;
    }

    public void setTargetID(long targetID) {
        this.targetID = targetID;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NGACRelationship)) {
            return false;
        }

        NGACRelationship r = (NGACRelationship) o;
        return this.sourceID == r.sourceID
                && this.targetID == r.targetID;
    }
}
