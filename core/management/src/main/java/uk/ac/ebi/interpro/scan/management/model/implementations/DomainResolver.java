package uk.ac.ebi.interpro.scan.management.model.implementations;

import java.util.*;

public class DomainResolver {
    private final Map<Integer, Set<Integer>> graph;
    private final List<Set<Integer>> sets;

    public DomainResolver(Map<Integer, Set<Integer>> graph) {
        this.graph = graph;
        this.sets = new ArrayList<>();
    }

    public List<Set<Integer>> resolve() {
        makeSets(new ArrayList<>(), new ArrayList<>(this.graph.keySet()));
        return this.sets;
    }

    private void makeSets(List<Integer> currentSet, List<Integer> remainingNodes) {
        if (isValid(currentSet)) {
            if (remainingNodes.isEmpty()) {
                this.sets.add(new HashSet<>(currentSet));
                return;
            }
        } else {
            return;
        }

        int currentNode = remainingNodes.get(0);
        remainingNodes = remainingNodes.subList(1, remainingNodes.size());

        makeSets(new ArrayList<>(currentSet) {{
            add(currentNode);
        }}, remainingNodes);
        makeSets(new ArrayList<>(currentSet), remainingNodes);
    }

    private boolean isValid(List<Integer> candidate) {
        for (int nodeA : candidate) {
            for (int nodeB : candidate) {
                if (nodeA != nodeB && !this.graph.get(nodeB).contains(nodeA)) {
                    return false;
                }
            }
        }
        return true;
    }
}
