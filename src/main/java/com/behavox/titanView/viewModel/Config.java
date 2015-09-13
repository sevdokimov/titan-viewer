package com.behavox.titanView.viewModel;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

/**
 *
 */
public class Config {

    private Comparator<String> propComparator = (o1, o2) -> 0;

    private final List<NodeType> nodeTypes = new ArrayList<>();

    public List<NodeType> getNodeTypes() {
        return nodeTypes;
    }

    public void addNodeType(@NotNull NodeType nodeType) {
        nodeTypes.add(nodeType);
    }

    public Comparator<String> getPropComparator() {
        return propComparator;
    }

    public void setPropertiesOrder(List<String> propOrder) {
        Map<String, Integer> map = new HashMap<>();

        int i = Integer.MAX_VALUE;
        for (String propName : propOrder) {
            map.put(propName, i--);
        }

        propComparator = Comparator.comparing(new Function<String, Integer>() {
            @Override
            public Integer apply(String s) {
                return map.get(s);
            }
        }, Comparator.nullsFirst(Comparator.<Integer>naturalOrder())).reversed();
    }
}
