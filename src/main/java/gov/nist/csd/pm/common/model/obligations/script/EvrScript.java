package gov.nist.csd.pm.common.model.obligations.script;

import gov.nist.csd.pm.common.model.obligations.EvrRule;

import java.util.ArrayList;
import java.util.List;

public class EvrScript {
    private String        scriptName;
    private boolean       enabled;
    private List<EvrRule> rules;

    public EvrScript(String scriptName) {
        this.scriptName = scriptName;
        this.rules = new ArrayList<>();
    }

    public EvrScript(String scriptName, boolean enabled) {
        this.scriptName = scriptName;
        this.rules = new ArrayList<>();
        this.enabled = enabled;
    }

    public String getScriptName() {
        return scriptName;
    }

    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    public List<EvrRule> getRules() {
        return rules;
    }

    public void setRules(List<EvrRule> rules) {
        this.rules = rules;
    }

    public void addRule(EvrRule rule) {
        this.rules.add(rule);
    }

    public EvrRule getRule(String ruleLabel) {
        for(EvrRule rule : rules) {
            if(rule.getLabel().equals(ruleLabel)) {
                return rule;
            }
        }

        return null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
