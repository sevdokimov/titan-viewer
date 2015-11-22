package com.behavox.hbaseView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;

/**
 *
 */
public class Utils {

    public static final Gson GSON = new Gson();

    public static final JsonParser GSON_PARSER = new JsonParser();

    public static final Gson BHEX_GSON = new GsonBuilder().registerTypeAdapter(byte[].class, new TypeAdapter<byte[]>() {

        @Override
        public void write(JsonWriter out, byte[] value) throws IOException {
            if (value == null) {
                out.nullValue();
                return;
            }

            out.value(toHex(value));
        }

        @Override
        public byte[] read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }

            return Bytes.fromHex(in.nextString());
        }
    }).create();

    private Utils() {
    }

    public static String toHex(byte[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2);

        for (byte b : data) {
            int x = (b & 0xFF) >> 4;
            sb.append((char) ((x > 9 ? 'A' - 10 : '0') + x));
            x = b & 0x0F;
            sb.append((char) ((x > 9 ? 'A' - 10 : '0') + x));
        }

        return sb.toString();
    }

}
