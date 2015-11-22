package com.behavox.hbaseView;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.SingleColumnValueFilter;
import org.apache.hadoop.hbase.util.Bytes;

import javax.annotation.Nonnull;

/**
 *
 */
public class FilterUtils {

    public static Filter toHBaseFilter(@Nonnull String filter) {
        JsonElement jsonElement = Utils.GSON_PARSER.parse(filter);

        return toFilter((JsonObject) jsonElement);
    }

    private static Filter toFilter(JsonObject element) {
        String cls = element.get("cls").getAsString();

        switch (cls) {
            case "colVal":
                return parseColVal(element);

            default:
                throw new IllegalArgumentException("Unknown filter type: " + cls);
        }
    }

    private static SingleColumnValueFilter parseColVal(@Nonnull JsonObject obj) {
        CompareFilter.CompareOp op = CompareFilter.CompareOp.valueOf(obj.get("op").getAsString());

        byte[] val = Bytes.fromHex(obj.get("val").getAsString());

        String family = obj.get("family").getAsString();
        String q = obj.get("q").getAsString();

        return new SingleColumnValueFilter(Bytes.toBytes(family), Bytes.toBytes(q), op, val);
    }
}
