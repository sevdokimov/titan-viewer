package com.behavox.hbaseView.titan;

import com.behavox.hbaseView.titan.json.ShortVertexJson;
import com.behavox.hbaseView.titan.viewModel.*;
import com.thinkaurelius.titan.core.EdgeLabel;
import com.thinkaurelius.titan.core.TitanEdge;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.TitanVertex;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import org.jetbrains.annotations.NotNull;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class TitanUtils {

    private TitanUtils() {

    }

    public static ShortVertexJson format(@NotNull TitanVertex v) {
        TitanConfig cfg = TitanConfigManager.getInstance().getConfig();

        ElementType<TitanVertex, ShortVertexJson> type = cfg.findType(v);

        ElementFormatter<TitanVertex, ShortVertexJson> formatter = type != null
                ? type.getFormatter()
                : DefaultVertexFormatter.INSTANCE;

        return formatter.format(v);
    }

    public static String format(@NotNull TitanEdge edge) {
        TitanConfig cfg = TitanConfigManager.getInstance().getConfig();

        ElementType<TitanEdge, String> type = cfg.findType(edge);

        ElementFormatter<TitanEdge, String> formatter = type != null ? type.getFormatter() : DefaultEdgeFormatter.INSTANCE;

        return formatter.format(edge);
    }

    public static List<String> getEdgeLabels(TitanGraph g) {
        List<String> res = new ArrayList<>();

        TitanManagement mngm = g.getManagementSystem();

        try {
            for (EdgeLabel edgeLabel : mngm.getRelationTypes(EdgeLabel.class)) {
                res.add(edgeLabel.getName());
            }
        } finally {
            mngm.rollback();
        }

        return res;
    }

    public static String toString(Throwable t) {
        StringWriter stringWriter = new StringWriter();

        try (PrintWriter pw = new PrintWriter(stringWriter)) {
            t.printStackTrace(pw);
        }

        return stringWriter.toString();
    }
}
