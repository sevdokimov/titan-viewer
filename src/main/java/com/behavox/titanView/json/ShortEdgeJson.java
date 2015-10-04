package com.behavox.titanView.json;

import com.behavox.titanView.Utils;
import com.thinkaurelius.titan.core.TitanEdge;
import com.tinkerpop.blueprints.Direction;
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

    public ShortEdgeJson(@NotNull TitanEdge edge) {
        outV = Utils.format(edge.getVertex(Direction.OUT));
        inV = Utils.format(edge.getVertex(Direction.IN));

        id = edge.getLongId();
        fullId = edge.getId().toString();

        label = edge.getLabel();

        body = Utils.format(edge);
    }
}
