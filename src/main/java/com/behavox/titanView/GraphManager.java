package com.behavox.titanView;

import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GraphManager {

    private static final String DB_HOST = "127.0.0.1";

    private static final ImmutableSet<String> SHORT_TITAN_COLUMNS = ImmutableSet.of("e", "f", "g", "h", "i", "l", "m", "s", "t");
    private static final ImmutableSet<String> LONG_TITAN_COLUMNS = ImmutableSet.of("edgestore", "edgestore_lock_", "graphindex", "graphindex_lock_",
            "system_properties", "system_properties_lock_", "systemlog", "titan_ids", "txlog");

    private static final GraphManager instance = new GraphManager();

    private final Map<String, CompletableFuture<TitanGraph>> graphMap = new ConcurrentHashMap<>();

    private HBaseAdmin admin;

    @NotNull
    public TitanGraph getGraph(@NotNull String tableName) {
        while (true) {
            CompletableFuture<TitanGraph> future = graphMap.get(tableName);

            if (future == null) {
                future = new CompletableFuture<>();

                CompletableFuture<TitanGraph> old = graphMap.putIfAbsent(tableName, future);

                if (old == null) {
                    try {
                        TitanGraph titanGraph = TitanFactory.build().set("storage.backend", "hbase").set("storage.hbase.table", tableName).open();

                        future.complete(titanGraph);

                        return titanGraph;
                    } catch (Throwable t) {
                        future.completeExceptionally(t);

                        throw t;
                    }
                }

                future = old;
            }

            TitanGraph res;

            try {
                res = future.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException();
            } catch (ExecutionException e) {
                throw Throwables.propagate(e.getCause());
            }

            if (res.isOpen()) {
                return res;
            }

            graphMap.remove(tableName, future);
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

    public synchronized HBaseAdmin getAdmin() throws IOException {
        if (admin == null) {
            Configuration hbaseCfg = HBaseConfiguration.create();
            hbaseCfg.setInt("timeout", 120000);
            hbaseCfg.set("hbase.master", "*" + DB_HOST + ":9000*");
            hbaseCfg.set("hbase.zookeeper.quorum", DB_HOST);
            hbaseCfg.set("hbase.zookeeper.property.clientPort", "2181");

            admin = new HBaseAdmin(hbaseCfg);
        }

        return admin;
    }

    public String getHost() {
        return DB_HOST;
    }

    public List<String> loadTables() throws IOException {
        List<String> res = new ArrayList<>();

        HBaseAdmin admin = getAdmin();

        for (TableName tableName : admin.listTableNames()) {
            if (isTitanTable(admin.getTableDescriptor(tableName))) {
                res.add(tableName.getNameAsString());
            }
        }

        return res;
    }
}
