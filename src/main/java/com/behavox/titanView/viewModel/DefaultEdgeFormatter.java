package com.behavox.titanView.viewModel;

import com.google.common.html.HtmlEscapers;
import com.thinkaurelius.titan.core.TitanEdge;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 */
public class DefaultEdgeFormatter implements ElementFormatter<TitanEdge, String> {

    public static final ElementFormatter<TitanEdge, String> INSTANCE = new DefaultEdgeFormatter();

    @NotNull
    @Override
    public String format(@NotNull TitanEdge edge) {
        Map<String, Object> propMap = new LinkedHashMap<>();

        for (String propName : edge.getPropertyKeys()) {
            if (propName.equals("name"))
                continue;

            propMap.put(propName, edge.getProperty(propName));
        }

        if (propMap.isEmpty()) {
            return "<span class='notFound'><small>No properties</small></span>";
        }

        StringBuilder res = new StringBuilder();

        if (propMap.size() < 3) {
            boolean first = true;
            for (Map.Entry<String, Object> entry : propMap.entrySet()) {
                if (first) {
                    first = false;
                }
                else {
                    res.append(", ");
                }

                res.append(HtmlEscapers.htmlEscaper().escape(entry.getKey())).append("=");

                String val = entry.getValue().toString();

                if (val.length() > 20)
                    val = val.substring(0, 20) + "..";

                res.append(HtmlEscapers.htmlEscaper().escape(val));
            }
        }
        else {
            int i = 0;
            for (Map.Entry<String, Object> entry : propMap.entrySet()) {
                if (i > 0)
                    res.append(", ");

                String val = entry.getValue().toString();
                if (val.length() > 200)
                    val = val.substring(0, 200) + "..";

                res.append("<span title='").append(HtmlEscapers.htmlEscaper().escape(val)).append("'>");
                res.append(HtmlEscapers.htmlEscaper().escape(entry.getKey()));
                res.append("</span>");

                if (++i > 4) {
                    res.append("...");
                    break;
                }

            }
        }

        return res.toString();
    }
}
