package com.behavox.hbaseView.json;

import org.jetbrains.annotations.NotNull;

/**
 *
 */
public class ObjectJson {

    private final String _type_ = "O";

    private final String cls;
    private final String toString;

    public ObjectJson(@NotNull Object o) {
        this.cls = o.getClass().getName();
        this.toString = o.toString();
    }
}
