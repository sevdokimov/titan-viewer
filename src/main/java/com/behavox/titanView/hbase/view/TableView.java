package com.behavox.titanView.hbase.view;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class TableView {

    private final String tableName;

    private final KeySettings key = new KeySettings();

    private final Map<String, ColumnSettings> columns = new HashMap<>();

    public TableView(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public ColumnSettings getOrCreateColumnSettings(@Nonnull String family, @Nonnull String q) {
        assert Thread.holdsLock(this);

        String key = family + ':' + q;

        ColumnSettings res = columns.get(key);

        if (res == null) {
            res = new ColumnSettings();

            columns.put(key, res);
        }

        return res;
    }

    public KeySettings getKey() {
        return key;
    }

    public static class ColumnSettings {
        private String rendererName;

        private String rendererAttr = "{}";

        public String getRendererName() {
            return rendererName;
        }

        public void setRendererName(String rendererName) {
            this.rendererName = rendererName;
        }

        public String getRendererAttr() {
            return rendererAttr;
        }

        public void setRendererAttr(String rendererAttr) {
            this.rendererAttr = rendererAttr;
        }
    }

    public static class KeySettings {
        private String rendererName;

        private String rendererAttr = "{}";

        public String getRendererName() {
            return rendererName;
        }

        public void setRendererName(String rendererName) {
            this.rendererName = rendererName;
        }

        public String getRendererAttr() {
            return rendererAttr;
        }

        public void setRendererAttr(String rendererAttr) {
            this.rendererAttr = rendererAttr;
        }
    }
}
