package io.inblocks.civicpower.cryptopolitics.models.transformations;

import io.inblocks.civicpower.cryptopolitics.models.Card;
import io.inblocks.civicpower.cryptopolitics.models.Context;
import io.inblocks.civicpower.cryptopolitics.models.Talon;
import io.inblocks.civicpower.cryptopolitics.models.Transformation;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Introspected
public class AddCardsTransformation implements Transformation {

    @Valid @NotNull
    public final List<Card> cards;

    public AddCardsTransformation(final List<Card> cards) {
        this.cards = cards;
    }

    @Override
    public Talon apply(final Context context, final Talon in) {
        Talon accumulator = in;
        for (Card card : cards) {
            accumulator = accumulator.addCard(card);
        }
        return accumulator;
    }
}
