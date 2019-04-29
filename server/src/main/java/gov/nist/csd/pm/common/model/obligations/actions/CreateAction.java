package gov.nist.csd.pm.common.model.obligations.actions;

import gov.nist.csd.pm.common.model.obligations.EvrNode;
import gov.nist.csd.pm.common.model.obligations.functions.Function;

import java.util.List;
import java.util.Map;

public class CreateAction implements Action {
    private String              name;
    private String              type;
    private Map<String, String> properties;
    private Function            function;
    private List<EvrNode>       containers;

    public CreateAction() {

    }

    public CreateAction(String name, String type, Map<String, String> properties) {
        this.name = name;
        this.type = type;
        this.properties = properties;
    }

    public CreateAction(Function function) {
        this.function = function;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public Function getFunction() {
        return function;
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public List<EvrNode> getContainers() {
        return containers;
    }

    public void setContainers(List<EvrNode> containers) {
        this.containers = containers;
    }
}
