package com.behavox.hbaseView.titan.viewModel;

import com.behavox.hbaseView.titan.ConfigManager;
import com.behavox.hbaseView.titan.json.ShortVertexJson;
import com.google.common.html.HtmlEscapers;
import com.thinkaurelius.titan.core.TitanVertex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 */
public class DefaultVertexFormatter implements ElementFormatter<TitanVertex, ShortVertexJson> {

    public static final ElementFormatter INSTANCE = new DefaultVertexFormatter();

    @NotNull
    @Override
    public ShortVertexJson format(@NotNull TitanVertex v) {
        List<String> propertyKeys = new ArrayList<>(v.getPropertyKeys());
        propertyKeys.sort(ConfigManager.getInstance().getConfig().getPropComparator());

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        for (String propertyName : propertyKeys) {
            map.put(propertyName, v.getProperty(propertyName));
        }

        return new ShortVertexJson(v.getLongId(), null, HtmlEscapers.htmlEscaper().escape(map.toString()));
    }
}
