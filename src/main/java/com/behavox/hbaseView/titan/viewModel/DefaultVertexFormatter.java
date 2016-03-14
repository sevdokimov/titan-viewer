package com.behavox.hbaseView.titan.viewModel;

import com.behavox.hbaseView.titan.TitanConfigManager;
import com.behavox.hbaseView.titan.json.ShortVertexJson;
import com.google.common.html.HtmlEscapers;
import com.thinkaurelius.titan.core.TitanVertex;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 */
public class DefaultVertexFormatter implements ElementFormatter<Vertex, ShortVertexJson> {

    public static final ElementFormatter INSTANCE = new DefaultVertexFormatter();

    @NotNull
    @Override
    public ShortVertexJson format(@NotNull Vertex v) {
        List<String> propertyKeys = new ArrayList<>(v.keys());
        propertyKeys.sort(TitanConfigManager.getInstance().getConfig().getPropComparator());

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        for (String propertyName : propertyKeys) {
            map.put(propertyName, v.property(propertyName));
        }

        return new ShortVertexJson(((TitanVertex)v).longId(), null, HtmlEscapers.htmlEscaper().escape(map.toString()));
    }
}
