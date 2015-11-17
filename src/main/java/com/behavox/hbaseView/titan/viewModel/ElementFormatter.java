package com.behavox.hbaseView.titan.viewModel;

import com.thinkaurelius.titan.core.TitanElement;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public interface ElementFormatter<T extends TitanElement, R> {

    @NotNull
    R format(@NotNull T v);

}
