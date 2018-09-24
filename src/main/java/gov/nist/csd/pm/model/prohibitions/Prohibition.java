package gov.nist.csd.pm.model.prohibitions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Prohibition  implements Serializable {
    private ProhibitionSubject        subject;
    private List<ProhibitionResource> resources;
    private String                    name;
    private HashSet<String>           operations;
    private boolean                   intersection;

    public Prohibition(){

    }

    public Prohibition(ProhibitionSubject subject, List<ProhibitionResource> resources, String name, HashSet<String> operations, boolean intersection) {
        this.subject = subject;
        if(resources == null){
            this.resources = new ArrayList<>();
        }else {
            this.resources = resources;
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

    public List<ProhibitionResource> getResources() {
        return resources;
    }

    public void addResource(ProhibitionResource resource){
        resources.add(resource);
    }

    public void removeResource(long resourceId){
        for(ProhibitionResource dr : resources){
            if(dr.getResourceID() == resourceId){
                resources.remove(dr);
                return;
            }
        }
    }

    public void clearResources() {
        resources.clear();
    }

    public void setResources(List<ProhibitionResource> resources){
        this.resources = resources;
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
}
