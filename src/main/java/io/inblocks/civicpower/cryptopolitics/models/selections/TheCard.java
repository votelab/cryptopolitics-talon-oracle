package io.inblocks.civicpower.cryptopolitics.models.selections;

import io.inblocks.civicpower.cryptopolitics.models.Selection;
import io.inblocks.civicpower.cryptopolitics.models.SelectionResult;
import io.inblocks.civicpower.cryptopolitics.models.Talon;
import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotBlank;
import java.util.Random;

@Introspected
public record TheCard(@NotBlank String cardClass, @NotBlank String cardSerie, int orderNumber) implements Selection {
    @Override
    public SelectionResult pickCards(Talon talon, Random random) {
        return talon.pickSpecificCard(cardClass, cardSerie, orderNumber);
    }
}
