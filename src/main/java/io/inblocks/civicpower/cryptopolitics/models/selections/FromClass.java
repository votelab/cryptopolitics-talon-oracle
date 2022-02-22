package io.inblocks.civicpower.cryptopolitics.models.selections;

import io.inblocks.civicpower.cryptopolitics.models.Selection;
import io.inblocks.civicpower.cryptopolitics.models.SelectionResult;
import io.inblocks.civicpower.cryptopolitics.models.Talon;
import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotBlank;
import java.util.Random;

@Introspected
public record FromClass(@NotBlank String cardClass) implements Selection {
    @Override
    public SelectionResult pickCards(Talon talon, Random random) {
        return talon.pickCardByClass(cardClass, random.nextLong());
    }
}
