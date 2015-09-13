package com.behavox.titanView.json;

import com.behavox.titanView.Utils;
import com.thinkaurelius.titan.core.TitanProperty;
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
        label = v.getLabel();

        for (TitanProperty property : v.getProperties()) {
            props.put(property.getPropertyKey().getName(), property.getValue());
        }

        shortView = Utils.format(v);
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
