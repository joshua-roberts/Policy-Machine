package gov.nist.csd.pm.common.model.obligations;

import gov.nist.csd.pm.common.exceptions.PMException;
import gov.nist.csd.pm.common.model.graph.nodes.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EvrEntity {
    private Node                node;
    private String              name;
    private String              type;
    private Map<String, String> properties;
    private EvrFunction         function;
    private EvrProcess          process;
    private List<EvrEntity>     evrEntityList;
    private boolean             compliment;

    public EvrEntity() {

    }

    public boolean isClass() {
        if(type == null) {
            return false;
        }

        return type.equals("class");
    }

    //node
    public EvrEntity(Node node) {
        this.node = node;
    }

    public Node getNode() throws PMException {
        return node;
    }

    public boolean isNode() {
        return node != null;
    }

    //function
    public EvrEntity(EvrFunction function) {
        this.function = function;
    }

    public EvrFunction getFunction() {
        return this.function;
    }

    public boolean isFunction() {
        return function != null;
    }

    //node
    public EvrEntity(String name, String type, Map<String, String> properties, boolean comp) {
        this.name = name;
        this.type = type;
        this.properties = properties;
        this.compliment = comp;
    }

    public boolean isCompliment() {
        return compliment;
    }

    public void setCompliment(boolean compliment) {
        this.compliment = compliment;
    }

    public String getName() {
        if(name == null) {
            if(isNode()){
                return node.getName();
            }
        }

        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        if(type == null) {
            if(isNode()){
                return node.getType().toString();
            }
        }
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, String> getProperties() {
        if(properties == null) {
            if(isNode()) {
                return node.getProperties();
            }
        }

        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public boolean isEvrNode() {
        return name != null || type != null || properties != null;
    }

    //process
    public EvrEntity(EvrProcess process) {
        this.process = process;
    }

    public EvrProcess getProcess() {
        return process;
    }

    public boolean isProcess() {
        return process != null;
    }

    //value
    public boolean isValue() {
        return name != null && type == null;
    }

    public boolean isAny() {
        return node == null
                && name == null
                && type == null
                && (properties == null || properties.size() == 0)
                && process == null
                && function == null;
    }

    //list
    public EvrEntity(List<EvrEntity> evrEntityList) {
        this.evrEntityList = evrEntityList;
    }

    public void addEntity(EvrEntity evrEntity) {
        if(evrEntityList == null) {
            evrEntityList = new ArrayList<>();
        }

        evrEntityList.add(evrEntity);
    }

    public List<EvrEntity> getEntityList() {
        return evrEntityList;
    }

    public boolean isList() {
        return evrEntityList != null;
    }

    public boolean equals(Object o) {
        if(!(o instanceof EvrEntity)) {
            return false;
        }

        EvrEntity entity = (EvrEntity) o;

        if(this.isAny() && entity.isAny()) {
            return true;
        }

        //check node
        if(this.isNode()) {
            return this.getName().equals(entity.getName()) &&
                    this.getType().equals(entity.getType()) &&
                    this.getProperties().equals(entity.getProperties());
        }

        //check function
        if(this.isFunction()) {
            if(entity.isFunction()) {
                return this.getFunction().equals(entity.getFunction());
            } else {
                return false;
            }
        }

        //check process
        if(this.isProcess()) {
            return this.getProcess().equals(entity.getProcess());
        }

        return false;
    }
}
