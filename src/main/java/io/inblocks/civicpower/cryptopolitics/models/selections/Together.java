package io.inblocks.civicpower.cryptopolitics.models.selections;

import io.inblocks.civicpower.cryptopolitics.models.cards.Card;
import io.inblocks.civicpower.cryptopolitics.models.Selection;
import io.inblocks.civicpower.cryptopolitics.models.SelectionResult;
import io.inblocks.civicpower.cryptopolitics.models.cards.Talon;
import io.micronaut.core.annotation.Introspected;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Introspected
public record Together(
        @Valid @NotNull List<Selection> selections) implements Selection {

    public Together(Selection... selections) {
        this(List.of(selections));
    }

    @Override
    public SelectionResult pickCards(Talon talon, Random random) {
        final List<Card> cards = new ArrayList<>();
        Talon accumulator = talon;
        for (Selection selection : selections) {
            SelectionResult selectionResult = selection.pickCards(accumulator, random);
            cards.addAll(selectionResult.cards);
            accumulator = selectionResult.remainingCards;
        }
        return SelectionResult.builder().cards(cards).remainingCards(accumulator).build();
    }
}
