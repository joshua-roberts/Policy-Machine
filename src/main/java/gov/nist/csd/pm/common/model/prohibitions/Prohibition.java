package gov.nist.csd.pm.common.model.prohibitions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Object representing a Prohibition.
 */
public class Prohibition  implements Serializable {
    /**
     * The name of the prohibition.
     */
    private String                    name;

    /**
     * The subject of the prohibition.
     */
    private ProhibitionSubject        subject;

    /**
     * The list of nodes that the prohibition is applied to.
     */
    private List<ProhibitionNode> nodes;

    /**
     * The set of operations being prohibited.
     */
    private HashSet<String>           operations;

    /**
     * Whether this prohibition is applied to the intersection of all the nodes or not.
     */
    private boolean                   intersection;

    public Prohibition(){
        this.nodes = new ArrayList<>();
    }

    public Prohibition(String name, ProhibitionSubject subject, List<ProhibitionNode> nodes, HashSet<String> operations, boolean intersection) {
        if(subject == null) {
            throw new IllegalArgumentException("Prohibition subject cannot be null");
        }
        this.subject = subject;
        if(nodes == null){
            this.nodes = new ArrayList<>();
        } else {
            this.nodes = nodes;
        }
        this.name = name;
        this.operations = operations;
        this.intersection = intersection;
    }

    public ProhibitionSubject getSubject() {
        return subject;
    }

    public void setSubject(ProhibitionSubject subject) {
        this.subject = subject;
    }

    public List<ProhibitionNode> getNodes() {
        return nodes;
    }

    public void addNode(ProhibitionNode node){
        nodes.add(node);
    }

    public void removeNode(long id){
        for(ProhibitionNode dr : nodes){
            if(dr.getID() == id){
                nodes.remove(dr);
                return;
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashSet<String> getOperations() {
        return operations;
    }

    public void setOperations(HashSet<String> operations) {
        this.operations = operations;
    }

    public boolean isIntersection() {
        return intersection;
    }

    public void setIntersection(boolean intersection) {
        this.intersection = intersection;
    }

    public boolean equals(Object o) {
        if(!(o instanceof Prohibition)) {
            return false;
        }

        Prohibition p = (Prohibition)o;
        return this.getName().equals(p.getName());
    }

    public int hashCode() {
        return Objects.hash(name);
    }
}
