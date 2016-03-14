package com.behavox.hbaseView.titan.json;

import com.behavox.hbaseView.titan.TitanUtils;
import com.thinkaurelius.titan.core.TitanVertex;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class FullVertexJson {

    private String label;

    private final Map<String, Object> props = new HashMap<>();

    private ShortVertexJson shortView;

    public FullVertexJson(@NotNull TitanVertex v) {
        label = v.label();

        for (String propertyName : v.keys()) {
            props.put(propertyName, v.property(propertyName).value());
        }

        shortView = TitanUtils.format(v);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public ShortVertexJson getShortView() {
        return shortView;
    }

    public Map<String, Object> getProperties() {
        return props;
    }
}
