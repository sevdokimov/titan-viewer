package com.behavox.titanView.viewModel;

import com.behavox.titanView.json.ShortVertexJson;
import com.google.common.base.Strings;
import com.google.common.html.HtmlEscapers;
import com.thinkaurelius.titan.core.TitanVertex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public class StringFormatter implements ElementFormatter<TitanVertex, ShortVertexJson> {

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
    public ShortVertexJson format(@NotNull TitanVertex v) {
        String[] values = new String[props.size()];

        for (int i = 0; i < props.size(); i++) {
            values[i] = HtmlEscapers.htmlEscaper().escape(Strings.nullToEmpty(v.getProperty(props.get(i))));
        }

        return new ShortVertexJson(v.getLongId(), cls, String.format(format, values));
    }
}
