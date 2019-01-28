package gov.nist.csd.pm.pap.search;

import gov.nist.csd.pm.common.model.graph.Search;
import gov.nist.csd.pm.common.model.graph.nodes.Node;
import gov.nist.csd.pm.pap.graph.MemGraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MemGraphSearch implements Search {

    private HashMap<Long, Node> nodes;
    public MemGraphSearch(MemGraph graph) {
        if(graph == null) {
            throw new IllegalArgumentException("nodes to search in cannot be null");
        }
        this.nodes = graph.getNodesMap();
    }

    @Override
    public HashSet<Node> search(String name, String type, Map<String, String> properties) {
        HashSet<Node> results = new HashSet<>();
        // iterate over the nodes to find ones that match the search parameters
        for(Long id : nodes.keySet()) {
            Node node = nodes.get(id);

            // if the name parameter is not null and the current node name does not equal the name parameter, do not add
            if (name != null && !node.getName().equals(name)) {
                continue;
            }

            // if the type parameter is not null and the current node type does not equal the type parameter, do not add
            if (type != null && !node.getType().toString().equals(type)) {
                continue;
            }

            boolean add = true;
            if (properties != null) {
                for (String key : properties.keySet()) {
                    String checkValue = properties.get(key);
                    String foundValue = node.getProperties().get(key);
                    // if the property provided in the search parameters is null or *, continue to the next property
                    if(checkValue == null || checkValue.equals("*")) {
                        continue;
                    }
                    if(foundValue == null || !foundValue.equals(checkValue)) {
                        add = false;
                        break;
                    }
                }
            }

            if (add) {
                results.add(node);
            }
        }

        return results;
    }

    @Override
    public Node getNode(long id) {
        return nodes.get(id);
    }
}
