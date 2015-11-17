package com.behavox.hbaseView.titan.json;

import com.behavox.hbaseView.titan.TitanUtils;
import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanVertex;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class HalfEdgeJson {

    private final String id;

    private final String label;

    private final ShortVertexJson shortView;

    public HalfEdgeJson(@NotNull TitanVertex src, @NotNull TitanEdge edge) {
        id = edge.getId().toString();

        label = edge.getLabel();

        this.shortView = TitanUtils.format(edge.getOtherVertex(src));
    }

    public String getId() {
        return id;
    }

    public ShortVertexJson getShortView() {
        return shortView;
    }

    public String getLabel() {
        return label;
    }
}
