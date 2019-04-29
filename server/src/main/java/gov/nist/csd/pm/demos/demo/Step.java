package gov.nist.csd.pm.demos.demo;

import java.util.List;

public class Step {
    private   String       description;
    private   List<String> actions;
    private   String       consequence;

    public Step() {}

    public Step(String description, List<String> actions, String consequence) {
        this.description = description;
        this.actions = actions;
        this.consequence = consequence;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public String getConsequence() {
        return consequence;
    }

    public void setConsequence(String consequence) {
        this.consequence = consequence;
    }
}
