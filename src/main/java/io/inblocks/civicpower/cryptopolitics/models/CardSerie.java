package io.inblocks.civicpower.cryptopolitics.models;

import io.inblocks.civicpower.cryptopolitics.exceptions.CardAlreadyPresent;
import io.inblocks.civicpower.cryptopolitics.exceptions.NoSuchCard;
import io.micronaut.core.annotation.Introspected;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.Objects;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Introspected
@Builder(toBuilder = true)
public class CardSerie {
    @NotNull public final String name;
    public final int size;
    @Schema(description = "Base64 encoding of the bitmap of cards present", implementation = String.class, example = "A////w==")
    @NotNull public final BigInteger setBitmap; // BitSet another possibility
    @NotNull public final Integer initialDealIndex;

    public CardSerie(final String name, final int size) {
        this.name = name;
        this.size = size;
        setBitmap = BigInteger.ONE.shiftLeft(size).subtract(BigInteger.ONE);
        initialDealIndex = 0;
    }

    public CardSerie removeCard(final int orderNumber) throws NoSuchCard {
        checkOrderNumber(orderNumber);
        final int index = orderNumber - 1;
        if (!setBitmap.testBit(index))
            throw new NoSuchCard(orderNumber);
        return toBuilder().setBitmap(setBitmap.clearBit(index)).build();
    }

    public CardSerie addCard(final int orderNumber) throws CardAlreadyPresent {
        checkOrderNumber(orderNumber);
        final int index = orderNumber - 1;
        if (setBitmap.testBit(index))
            throw new CardAlreadyPresent(orderNumber);
        return toBuilder().setBitmap(setBitmap.setBit(index)).build();
    }

    private void checkOrderNumber(int orderNumber) {
        if (orderNumber < 1 || orderNumber > size)
            throw new IllegalArgumentException("Card order number out of bounds");
    }

    @Data
    @Builder
    public static class PickNextCardResult {
        public final int cardOrderNumber;
        public final CardSerie remainingCards;
    }

    public PickNextCardResult pickNextCard() {
        if (initialDealIndex < size) {
            final int deal = initialDealIndex + 1;
            final CardSerie cardsLeft = removeCard(deal).toBuilder().initialDealIndex(deal).build();
            return PickNextCardResult.builder()
                    .cardOrderNumber(deal)
                    .remainingCards(cardsLeft)
                    .build();
        }
        else {
            final int deal = setBitmap.bitLength();
            final CardSerie cardsLeft = removeCard(deal);
            return PickNextCardResult.builder()
                    .cardOrderNumber(deal)
                    .remainingCards(cardsLeft)
                    .build();
        }
    }

    public int count() {
        return setBitmap.bitCount();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardSerie that = (CardSerie) o;
        return size == that.size && name.equals(that.name) && setBitmap.equals(that.setBitmap) && initialDealIndex.equals(that.initialDealIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, size, setBitmap, initialDealIndex);
    }
}
