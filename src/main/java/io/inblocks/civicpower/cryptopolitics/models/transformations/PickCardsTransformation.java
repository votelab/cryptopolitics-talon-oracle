package io.inblocks.civicpower.cryptopolitics.models.transformations;

import io.inblocks.civicpower.cryptopolitics.models.*;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Introspected
public class PickCardsTransformation implements Transformation {

    @NotNull public final List<String> cardClasses;

    private final List<Card> pickedCards;

    public PickCardsTransformation(final List<String> cardClasses) {
        this.cardClasses = cardClasses;
        pickedCards = new ArrayList<>();
    }

    @Override
    public Talon apply(final Context context, final Talon in) {
        Talon accumulator = in;
        for (String cardClass : cardClasses) {
            PickNextCardResult result = accumulator.pickNextCard(cardClass, context.getRandom().nextLong());
            pickedCards.add(result.card);
            accumulator = result.remainingCards;
        }
        return accumulator;
    }

    @Override
    public Object getResults() {
        return pickedCards;
    }
}
