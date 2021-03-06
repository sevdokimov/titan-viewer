package com.behavox.hbaseView.titan.viewModel;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 *
 */
public class TitanConfig {

    private Comparator<String> propComparator = (o1, o2) -> 0;

    private final List<ElementType> elementTypes = new ArrayList<>();

    @Nullable
    public <T extends Element, R> ElementType<T, R> findType(T element) {
        for (ElementType type : elementTypes) {
            if (type.getPredicate().test(element))
                return type;
        }

        return null;
    }

    public void addType(@NotNull ElementType type) {
        elementTypes.add(type);
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
