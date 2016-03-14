package com.behavox.hbaseView.titan.viewModel;

import org.apache.tinkerpop.gremlin.structure.Element;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public interface ElementFormatter<T extends Element, R> {

    @NotNull
    R format(@NotNull T v);

}
