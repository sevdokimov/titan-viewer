package com.behavox.hbaseView.titan.json;

import com.behavox.hbaseView.titan.TitanUtils;
import com.thinkaurelius.titan.core.TitanEdge;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class ShortEdgeJson {

    private final String _type_ = "E";

    private final long id;
    private final String fullId;

    private final String label;

    private final ShortVertexJson outV;

    private final ShortVertexJson inV;

    private final String body;

    public ShortEdgeJson(@NotNull Edge edge) {
        outV = TitanUtils.format(edge.outVertex());
        inV = TitanUtils.format(edge.inVertex());

        id = ((TitanEdge)edge).longId();
        fullId = edge.id().toString();

        label = edge.label();

        body = TitanUtils.format(edge);
    }
}
