package com.behavox.hbaseView.titan.viewModel;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

/**
 *
 */
public class PropertyPredicate implements Predicate<Vertex> {

    private String propName;

    private String value;

    public PropertyPredicate(@NotNull String propName, @NotNull String value) {
        this.propName = propName;
        this.value = value;
    }

    @Override
    public boolean test(Vertex vertex) {
        return value.equals(vertex.<String>property(propName).orElse(null));
    }
}
