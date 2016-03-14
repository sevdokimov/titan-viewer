package com.behavox.hbaseView.titan.viewModel;

import org.apache.tinkerpop.gremlin.structure.Element;

import java.util.function.Predicate;

/**
 *
 */
public class ElementType<T extends Element, R> {

    protected final Predicate<? super T> predicate;

    protected final ElementFormatter<T, R> formatter;

    public ElementType(Predicate<? super T> predicate, ElementFormatter<T, R> formatter) {
        this.predicate = predicate;
        this.formatter = formatter;
    }

    public Predicate<? super T> getPredicate() {
        return predicate;
    }

    public ElementFormatter<T, R> getFormatter() {
        return formatter;
    }
}
