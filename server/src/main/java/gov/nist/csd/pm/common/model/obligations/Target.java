package gov.nist.csd.pm.common.model.obligations;

import java.util.ArrayList;
import java.util.List;

public class Target {
    private List<String> policyElements;
    private List<String> containers;

    public Target() {
        this.policyElements = new ArrayList<>();
        this.containers = new ArrayList<>();
    }

    public List<String> getPolicyElements() {
        return policyElements;
    }

    public void setPolicyElements(List<String> policyElements) {
        this.policyElements = policyElements;
    }

    public List<String> getContainers() {
        return containers;
    }

    public void setContainers(List<String> containers) {
        this.containers = containers;
    }
}
