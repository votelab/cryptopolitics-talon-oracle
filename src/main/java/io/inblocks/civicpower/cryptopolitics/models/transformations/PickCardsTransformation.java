package io.inblocks.civicpower.cryptopolitics.models.transformations;

import io.inblocks.civicpower.cryptopolitics.models.*;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

@Data
@Introspected
public class PickCardsTransformation implements Transformation {

    @Valid @NotNull public final Selection selection;

    private List<Card> pickedCards;

    public PickCardsTransformation(final Selection selection) {
        this.selection = selection;
        pickedCards = Collections.emptyList();
    }

    @Override
    public Talon apply(final Context context, final Talon in) {
        SelectionResult result = selection.pickCards(in, context.getRandom());
        pickedCards = result.cards;
        return result.remainingCards;
    }

    @Override
    public Object getResults() {
        return pickedCards;
    }
}
