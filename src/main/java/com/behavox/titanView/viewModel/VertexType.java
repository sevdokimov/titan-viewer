package com.behavox.titanView.viewModel;

import com.behavox.titanView.json.ShortVertexJson;
import com.thinkaurelius.titan.core.TitanElement;
import com.thinkaurelius.titan.core.TitanVertex;
import com.tinkerpop.blueprints.Vertex;

import java.util.function.Predicate;

/**
 *
 */
public class VertexType extends ElementType<TitanVertex, ShortVertexJson> {

    public VertexType(Predicate<? super TitanVertex> predicate, ElementFormatter<TitanVertex, ShortVertexJson> formatter) {
        super(new Predicate<TitanElement>() {
            @Override
            public boolean test(TitanElement titanVertex) {
                return titanVertex instanceof TitanVertex && predicate.test((TitanVertex) titanVertex);
            }
        }, formatter);
    }
}
