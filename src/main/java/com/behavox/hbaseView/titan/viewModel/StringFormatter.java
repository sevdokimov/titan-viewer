package com.behavox.hbaseView.titan.viewModel;

import com.behavox.hbaseView.titan.json.ShortVertexJson;
import com.google.common.html.HtmlEscapers;
import com.thinkaurelius.titan.core.TitanVertex;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class StringFormatter implements ElementFormatter<Vertex, ShortVertexJson> {

    private final List<String> props = new ArrayList<>();

    private String format;

    private String cls;

    public StringFormatter(@Nullable String cls, @NotNull String format, String... propNames) {
        this.cls = cls;
        this.format = format;

        Collections.addAll(props, propNames);

        //noinspection ResultOfMethodCallIgnored
        String.format(format, Collections.nCopies(propNames.length, "").toArray()); // Validate format.
    }

    @NotNull
    @Override
    public ShortVertexJson format(@NotNull Vertex v) {
        String[] values = new String[props.size()];

        for (int i = 0; i < props.size(); i++) {
            values[i] = HtmlEscapers.htmlEscaper().escape(v.<String>property(props.get(i)).orElse(""));
        }

        return new ShortVertexJson(((TitanVertex)v).longId(), cls, String.format(format, values));
    }
}
