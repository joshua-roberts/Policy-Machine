package gov.nist.csd.pm.common.model.graph.relationships;

/**
 * This class will serve as a parent class for Assignments and Associations.  Both types of relations have a source node
 * and a target node.
 */
public class Relationship {
    long sourceID;
    long targetID;

    public Relationship() {

    }

    public Relationship(long sourceID, long targetID) {
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
        if (!(o instanceof Relationship)) {
            return false;
        }

        Relationship r = (Relationship) o;
        return this.sourceID == r.sourceID
                && this.targetID == r.targetID;
    }
}
