package com.behavox.titanView.json;

import com.behavox.titanView.Utils;
import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanVertex;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class ShortEdgeJson {

    private final String id;

    private final String label;

    private final ShortVertexJson shortView;

    public ShortEdgeJson(@NotNull TitanVertex src, @NotNull TitanEdge edge) {
        id = edge.getId().toString();

        label = edge.getLabel();

        this.shortView = Utils.format(edge.getOtherVertex(src));
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
