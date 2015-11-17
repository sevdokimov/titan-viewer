package com.behavox.hbaseView.viewModel;

import com.behavox.hbaseView.json.ShortEdgeJson;
import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanElement;

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
