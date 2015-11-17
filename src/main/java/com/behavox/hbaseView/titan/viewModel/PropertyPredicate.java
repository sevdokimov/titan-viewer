package com.behavox.hbaseView.titan.viewModel;

import com.tinkerpop.blueprints.Vertex;
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
        return value.equals(vertex.getProperty(propName));
    }
}
