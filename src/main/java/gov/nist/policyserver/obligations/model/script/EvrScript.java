package gov.nist.policyserver.obligations.model.script;

import gov.nist.policyserver.obligations.exceptions.EvrRuleDoesNotExist;
import gov.nist.policyserver.obligations.model.EvrRule;

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

    public EvrRule getRule(String ruleLabel) throws EvrRuleDoesNotExist {
        for(EvrRule rule : rules) {
            if(rule.getLabel().equals(ruleLabel)) {
                return rule;
            }
        }

        throw new EvrRuleDoesNotExist(ruleLabel);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
