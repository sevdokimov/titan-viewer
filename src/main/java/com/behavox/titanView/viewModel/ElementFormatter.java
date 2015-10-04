package com.behavox.titanView.viewModel;

import com.behavox.titanView.json.ShortVertexJson;
import com.thinkaurelius.titan.core.TitanElement;
import com.thinkaurelius.titan.core.TitanVertex;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public interface ElementFormatter<T extends TitanElement, R> {

    @NotNull
    R format(@NotNull T v);

}
