package io.inblocks.civicpower.cryptopolitics.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PickNextCardResult {
    public final Card card;
    public final Talon remainingCards;
}
