package gov.nist.csd.pm.common.model.obligations;

import java.util.ArrayList;
import java.util.List;

public class Obligation {
    private String     label;
    private List<Rule> rules;

    public Obligation() {
        this.rules = new ArrayList<>();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }
}
