package com.behavox.hbaseView.titan.viewModel;

import com.behavox.hbaseView.titan.json.ShortVertexJson;
import com.thinkaurelius.titan.core.TitanVertex;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.function.Predicate;

/**
 *
 */
public class VertexType extends ElementType<Vertex, ShortVertexJson> {

    public VertexType(Predicate<? super Vertex> predicate, ElementFormatter<Vertex, ShortVertexJson> formatter) {
        super(new Predicate<Element>() {
            @Override
            public boolean test(Element titanVertex) {
                return titanVertex instanceof TitanVertex && predicate.test((Vertex) titanVertex);
            }
        }, formatter);
    }
}
