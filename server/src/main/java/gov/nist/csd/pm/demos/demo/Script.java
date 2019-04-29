package gov.nist.csd.pm.demos.demo;

import java.util.List;

public class Script {
    private String     name;
    private String     description;
    private List<Step> steps;
    private List<String> targets;

    public Script() {}

    public Script(String name, String description, List<Step> steps, List<String> targets) {
        this.name = name;
        this.description = description;
        this.steps = steps;
        this.targets = targets;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public List<String> getTargets() {
        return targets;
    }

    public void setTargets(List<String> targets) {
        this.targets = targets;
    }
}
