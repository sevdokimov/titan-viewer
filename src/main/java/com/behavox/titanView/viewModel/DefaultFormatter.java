package com.behavox.titanView.viewModel;

import com.behavox.titanView.ConfigManager;
import com.behavox.titanView.json.ShortVertexJson;
import com.thinkaurelius.titan.core.TitanVertex;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 */
public class DefaultFormatter implements NodeFormatter {

    public static final NodeFormatter INSTANCE = new DefaultFormatter();

    @NotNull
    @Override
    public ShortVertexJson format(@NotNull TitanVertex v) {
        List<String> propertyKeys = new ArrayList<>(v.getPropertyKeys());
        propertyKeys.sort(ConfigManager.getInstance().getConfig().getPropComparator());

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        for (String propertyName : propertyKeys) {
            map.put(propertyName, v.getProperty(propertyName));
        }

        return new ShortVertexJson(v.getLongId(), null, map.toString());
    }
}
