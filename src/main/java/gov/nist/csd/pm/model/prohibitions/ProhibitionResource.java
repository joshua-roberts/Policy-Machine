package gov.nist.csd.pm.model.prohibitions;

import java.io.Serializable;

public class ProhibitionResource implements Serializable {
    long    resourceID;
    boolean complement;

    public ProhibitionResource(){}

    public ProhibitionResource(long resourceID, boolean complement) {
        this.resourceID = resourceID;
        this.complement = complement;
    }

    public long getResourceID() {
        return resourceID;
    }

    public void setResourceID(long ProhibitionResourceId) {
        this.resourceID = ProhibitionResourceId;
    }

    public boolean isComplement() {
        return complement;
    }

    public void setComplement(boolean complement) {
        this.complement = complement;
    }

    public int hashCode(){
        return (int) resourceID;
    }

    public boolean equals(Object o){
        if(o instanceof ProhibitionResource){
            ProhibitionResource n = (ProhibitionResource) o;
            return this.resourceID == n.resourceID;
        }
        return false;
    }
}
