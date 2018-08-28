package gov.nist.csd.pm.model.obligations.script.rule.event;

import gov.nist.csd.pm.model.obligations.EvrEntity;

import java.util.ArrayList;
import java.util.List;

public class EvrPolicies {
    private List<EvrEntity> pcs;
    private boolean         or;

    public EvrPolicies() {
        pcs = new ArrayList<>();
        or = true;
    }

    public List<EvrEntity> getPcs() {
        return pcs;
    }

    public void setPcs(List<EvrEntity> pcs) {
        this.pcs = pcs;
    }

    public boolean isOr() {
        return or;
    }

    public void setOr(boolean or) {
        this.or = or;
    }

    public void addEntity(EvrEntity evrEntity) {
        this.pcs.add(evrEntity);
    }

    public boolean isAny() {
        return pcs.isEmpty();
    }
}
