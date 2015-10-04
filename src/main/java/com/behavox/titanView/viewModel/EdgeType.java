package com.behavox.titanView.viewModel;

import com.behavox.titanView.json.ShortEdgeJson;
import com.behavox.titanView.json.ShortVertexJson;
import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanElement;
import com.thinkaurelius.titan.core.TitanVertex;
import com.tinkerpop.blueprints.Vertex;

import java.util.function.Predicate;

/**
 *
 */
public class EdgeType extends ElementType<TitanEdge, ShortEdgeJson> {

    public EdgeType(Predicate<TitanEdge> predicate, ElementFormatter<TitanEdge, ShortEdgeJson> formatter) {
        super(new Predicate<TitanElement>() {
            @Override
            public boolean test(TitanElement edge) {
                return edge instanceof TitanEdge && predicate.test((TitanEdge) edge);
            }
        }, formatter);
    }
}
