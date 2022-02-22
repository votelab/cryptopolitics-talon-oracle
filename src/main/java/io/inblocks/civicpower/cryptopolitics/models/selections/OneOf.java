package io.inblocks.civicpower.cryptopolitics.models.selections;

import io.inblocks.civicpower.cryptopolitics.models.Selection;
import io.inblocks.civicpower.cryptopolitics.models.SelectionResult;
import io.inblocks.civicpower.cryptopolitics.models.Talon;
import io.inblocks.civicpower.cryptopolitics.models.Weighted;
import io.micronaut.core.annotation.Introspected;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Random;

@Introspected
public record OneOf(
        @Valid @NotNull @NotEmpty List<Weighted<Selection>> weightedSelections) implements Selection {

    @Override
    public SelectionResult pickCards(Talon talon, Random random) {
        Selection selection = Weighted.select(weightedSelections, random.nextLong());
        return selection.pickCards(talon, random);
    }
}
