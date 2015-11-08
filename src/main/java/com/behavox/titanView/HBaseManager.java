package com.behavox.titanView;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.elasticsearch.common.base.Throwables;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
public class HBaseManager {

    private static final String DB_HOST = "127.0.0.1";

    private static final HBaseManager instance = new HBaseManager();

    private HBaseAdmin admin;

    private final ConcurrentMap<String, HTable> tables = new ConcurrentHashMap<>();

    public static HBaseManager getInstance() {
        return instance;
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

    public HTable getTable(@NotNull String tableName) {
        HTable hTable = tables.get(tableName);

        if (hTable == null) {
            try {
                hTable = new HTable(getAdmin().getConfiguration(), tableName);

                HTable existingTable = tables.putIfAbsent(tableName, hTable);
                if (existingTable != null) {
                    hTable.close();

                    hTable = existingTable;
                }
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }

        return hTable;
    }

    public String getHost() {
        return DB_HOST;
    }

}
