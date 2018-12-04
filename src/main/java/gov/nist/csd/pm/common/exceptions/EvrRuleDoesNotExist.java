package gov.nist.csd.pm.common.exceptions;

public class EvrRuleDoesNotExist extends Exception{
    public EvrRuleDoesNotExist(String ruleLabel) {
        super("Rule with label '" + ruleLabel + "' does not exist");
    }
}
