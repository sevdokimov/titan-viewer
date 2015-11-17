package com.behavox.hbaseView.titan;

import com.behavox.hbaseView.HBaseManager;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GraphManager {

    private static final Logger log = LoggerFactory.getLogger(GraphManager.class);

    private static final long CONNECTION_KEEP_TIME = Long.parseLong(System.getProperty("keepConnection", String.valueOf(7 * 60 * 1000)));

    private static final String DB_HOST = "127.0.0.1";

    private static final ImmutableSet<String> SHORT_TITAN_COLUMNS = ImmutableSet.of("e", "f", "g", "h", "i", "l", "m", "s", "t");
    private static final ImmutableSet<String> LONG_TITAN_COLUMNS = ImmutableSet.of("edgestore", "edgestore_lock_", "graphindex", "graphindex_lock_",
            "system_properties", "system_properties_lock_", "systemlog", "titan_ids", "txlog");

    private final Map<String, ConnectionHolder> graphMap = new ConcurrentHashMap<>();

    private HBaseAdmin admin;

    private static final Timer TIMER = new Timer("Graph connection killer");

    private static final GraphManager instance = new GraphManager();

    public GraphManager() {
        TIMER.schedule(new TimerTask() {
            @Override
            public void run() {
                long threshold = System.currentTimeMillis() - CONNECTION_KEEP_TIME;

                for (Map.Entry<String, ConnectionHolder> entry : graphMap.entrySet()) {
                    if (entry.getValue().lastAccess < threshold && entry.getValue().future.isDone()) {
                        if (graphMap.remove(entry.getKey(), entry.getValue())) {
                            TitanGraph graph = entry.getValue().future.getNow(null);

                            if (graph != null) {
                                log.info("Disconnect from graph by timeout: {}", entry.getKey());

                                graph.shutdown();
                            }
                        }
                    }
                }
            }
        }, 5000, 5000);
    }

    public boolean isGraphOpen(@NotNull String tableName) {
        return graphMap.containsKey(tableName);
    }

    @NotNull
    public TitanGraph getGraph(@NotNull String tableName) {
        while (true) {
            ConnectionHolder holder = graphMap.get(tableName);

            if (holder == null) {
                holder = new ConnectionHolder();

                ConnectionHolder old = graphMap.putIfAbsent(tableName, holder);

                if (old == null) {
                    try {
                        log.info("Connection to the graph: {}", tableName);

                        TitanGraph titanGraph = TitanFactory.build().set("storage.backend", "hbase").set("storage.hbase.table", tableName).open();

                        holder.lastAccess = System.currentTimeMillis();

                        holder.future.complete(titanGraph);

                        return titanGraph;
                    } catch (Throwable t) {
                        holder.future.completeExceptionally(t);

                        throw t;
                    }
                }

                holder = old;
            }

            TitanGraph res;

            try {
                res = holder.future.get();

                holder.lastAccess = System.currentTimeMillis();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException();
            } catch (ExecutionException e) {
                throw Throwables.propagate(e.getCause());
            }

            if (res.isOpen()) {
                return res;
            }

            graphMap.remove(tableName, holder);
        }
    }

    public static GraphManager getInstance() {
        return instance;
    }
    
    private static boolean isTitanTable(HTableDescriptor tableDescriptor) {
        Set<String> names = Stream.of(tableDescriptor.getColumnFamilies()).map(HColumnDescriptor::getNameAsString).collect(Collectors.toSet());

        if (names.containsAll(SHORT_TITAN_COLUMNS)) {
            return true;
        }

        if (names.containsAll(LONG_TITAN_COLUMNS)) {
            return true;
        }

        return false;
    }

    public String getHost() {
        return DB_HOST;
    }

    public List<String> loadTables() throws IOException {
        List<String> res = new ArrayList<>();

        HBaseAdmin admin = HBaseManager.getInstance().getAdmin();

        for (TableName tableName : admin.listTableNames()) {
            if (isTitanTable(admin.getTableDescriptor(tableName))) {
                res.add(tableName.getNameAsString());
            }
        }

        return res;
    }

    private static class ConnectionHolder {
        private final CompletableFuture<TitanGraph> future = new CompletableFuture<>();

        private volatile long lastAccess;
    }
}
