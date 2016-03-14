package com.behavox.hbaseView.titan.json;

import com.behavox.hbaseView.titan.TitanUtils;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class HalfEdgeJson {

    private final String id;

    private final String label;

    private final ShortVertexJson shortView;

    public HalfEdgeJson(@NotNull Vertex src, @NotNull Edge edge) {
        id = edge.id().toString();

        label = edge.label();

        if (src.equals(edge.inVertex())) {
            this.shortView = TitanUtils.format(edge.outVertex());
        }
        else {
            this.shortView = TitanUtils.format(edge.inVertex());
        }
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
