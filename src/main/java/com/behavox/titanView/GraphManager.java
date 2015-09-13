package com.behavox.titanView;

import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import org.jetbrains.annotations.NotNull;

public class GraphManager {

    private static final GraphManager instance = new GraphManager();

    private TitanGraph graph;

    @NotNull
    public TitanGraph getGraph() {
        return graph;
    }

    public void initGraph(String cfg) {
        this.graph = TitanFactory.open(cfg);
    }

    public static GraphManager getInstance() {
        return instance;
    }
}
