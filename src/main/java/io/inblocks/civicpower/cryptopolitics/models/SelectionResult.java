package io.inblocks.civicpower.cryptopolitics.models;

import io.inblocks.civicpower.cryptopolitics.models.cards.Card;
import io.inblocks.civicpower.cryptopolitics.models.cards.Talon;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SelectionResult {
    public final List<Card> cards;
    public final Talon remainingCards;
}
