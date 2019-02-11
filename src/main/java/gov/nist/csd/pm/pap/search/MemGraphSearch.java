package gov.nist.csd.pm.pap.search;

import gov.nist.csd.pm.common.model.graph.Search;
import gov.nist.csd.pm.common.model.graph.nodes.NodeContext;
import gov.nist.csd.pm.pap.graph.MemGraph;

import java.util.*;

import static gov.nist.csd.pm.common.constants.Properties.NAMESPACE_PROPERTY;

public class MemGraphSearch implements Search {

    /**
     * Data structure to store detailed node information.
     */
    private MemGraph memGraph;

    /**
     * Constructor for an in-memory graph search.
     * @param graph The MemGraph instance to use to search.
     */
    public MemGraphSearch(MemGraph graph) {
        if(graph == null) {
            throw new IllegalArgumentException("nodes to search in cannot be null");
        }
        this.memGraph = graph;
    }

    @Override
    public HashSet<NodeContext> search(String name, String type, Map<String, String> properties) {
        if(properties == null) {
            properties = new HashMap<>();
        }

        String namespace = properties.get(NAMESPACE_PROPERTY);
        if((name != null && !name.isEmpty())
                && (type != null && !type.isEmpty())
                && (namespace != null && !namespace.isEmpty())) {
            HashMap<String, Long> namespaceNames = memGraph.getNamespaceNames();
            String namespaceStr = namespace + ":" + name + ":" + type;
            Long id = namespaceNames.get(namespaceStr);
            if(id != null) {
                NodeContext node = getNode(id);
                return new HashSet<>(Collections.singleton(node));
            }
        }

        HashSet<NodeContext> results = new HashSet<>();
        // iterate over the nodes to find ones that match the search parameters
        for(NodeContext node : memGraph.getNodes()) {
            // if the name parameter is not null and the current node name does not equal the name parameter, do not add
            if (name != null && !node.getName().equals(name)) {
                continue;
            }

            // if the type parameter is not null and the current node type does not equal the type parameter, do not add
            if (type != null && !node.getType().toString().equals(type)) {
                continue;
            }

            boolean add = true;
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

            if (add) {
                results.add(node);
            }
        }

        return results;
    }

    @Override
    public NodeContext getNode(long id) {
        return memGraph.getNodesMap().get(id);
    }
}
