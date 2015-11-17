package com.behavox.hbaseView.hbase.view;

import com.behavox.hbaseView.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
public class HBaseConfigManager {

    private static final Logger log = LoggerFactory.getLogger(HBaseConfigManager.class);

    private static final HBaseConfigManager INSTANCE = new HBaseConfigManager();

    private final ConcurrentMap<String, TableView> tableMap = new ConcurrentHashMap<>();

    private static volatile File configDir;

    public static HBaseConfigManager getInstance() {
        return INSTANCE;
    }

    public TableView getOrCreateTableView(String tableName) {
        TableView res = tableMap.get(tableName);

        if (res == null) {
            res = tableMap.computeIfAbsent(tableName, t -> {
                File configFile = getTableConfigFile(t);

                if (configFile.exists()) {
                    try {
                        try (FileReader reader = new FileReader(configFile)) {
                            return Utils.GSON.fromJson(reader, TableView.class);
                        }
                    } catch (IOException e) {
                        log.error("Failed to load table config " + configFile, e);
                    }
                }

                return new TableView(t);
            });
        }

        return res;
    }

    private static File getTableConfigFile(String tableName) {
        return new File(getConfigDir(), "table_" + tableName + ".json");
    }

    public void saveTableView(TableView tableView) {
        assert tableMap.values().contains(tableView);

        try {
            try (FileWriter wr = new FileWriter(getTableConfigFile(tableView.getTableName()))) {
                Utils.GSON.toJson(tableView, wr);
            }
        } catch (IOException e) {
            log.error("Failed to save table config: " + tableView.getTableName(), e);
        }
    }

    public static File getConfigDir() {
        File configDir = HBaseConfigManager.configDir;

        if (configDir == null) {
            String configDirStr = System.getProperty("config.dir");

            if (configDirStr != null) {
                configDir = new File(configDirStr);
            }
            else {
                String userHome = System.getProperty("user.home");

                if (userHome == null)
                    throw new RuntimeException("System.getProperty(\"user.home\") == null");

                if (!new File(userHome).isDirectory()) {
                    throw new RuntimeException("User home is not a directory: " + userHome);
                }

                configDir = new File(userHome, ".hbaseViewer");

                if (!configDir.exists()) {
                    if (!configDir.mkdir()) {
                        throw new RuntimeException("Failed to create config directory: " + configDir);
                    }
                }
                else if (!configDir.isDirectory()) {
                    throw new RuntimeException("Config directory is not a directory: " + configDir);
                }
            }

            HBaseConfigManager.configDir = configDir;
        }

        return configDir;
    }
}
