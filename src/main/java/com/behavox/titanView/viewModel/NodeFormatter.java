package com.behavox.titanView.viewModel;

import com.behavox.titanView.json.ShortVertexJson;
import com.thinkaurelius.titan.core.TitanVertex;
import org.jetbrains.annotations.NotNull;

/**
 *
 */
public interface NodeFormatter {

    @NotNull
    ShortVertexJson format(@NotNull TitanVertex v);

}
