package com.behavox.titanView.json;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 *
 */
public class ShortVertexJson {

    private final long id;

    private final String cls;

    private final String body;

    public ShortVertexJson(long id, @Nullable String cls, @NotNull String body) {
        this.id = id;
        this.cls = cls;
        this.body = body;
    }

    @Nullable
    public String getCls() {
        return cls;
    }

    public String getBody() {
        return body;
    }

    public long getId() {
        return id;
    }
}
