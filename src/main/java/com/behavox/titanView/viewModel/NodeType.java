package com.behavox.titanView.viewModel;

import com.tinkerpop.blueprints.Vertex;

import java.util.function.Predicate;

/**
 *
 */
public class NodeType {

    private final Predicate<Vertex> predicate;

    private final NodeFormatter formatter;

    public NodeType(Predicate<Vertex> predicate, NodeFormatter formatter) {
        this.predicate = predicate;
        this.formatter = formatter;
    }

    public Predicate<Vertex> getPredicate() {
        return predicate;
    }

    public NodeFormatter getFormatter() {
        return formatter;
    }
}
