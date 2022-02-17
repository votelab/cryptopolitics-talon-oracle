package io.inblocks.civicpower.cryptopolitics.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.inblocks.civicpower.cryptopolitics.ListUtils;
import io.inblocks.civicpower.cryptopolitics.exceptions.CardClassEmpty;
import io.inblocks.civicpower.cryptopolitics.exceptions.CardClassFinitudeMismatch;
import io.inblocks.civicpower.cryptopolitics.exceptions.NoSuchCardSerie;
import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@Data
@Introspected
@Builder(toBuilder = true)
public class CardClass {
    @NotNull
    public final String cardClass;
    @JsonProperty(value="infinite")
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    protected final boolean isInfinite;
    @NotNull
    public final List<CardSerie> series;

    static final long LONG_MASK = 0xffffffffL;

    public CardClass(final String cardClass, final boolean isInfinite, final List<CardSerie> series) {
        this.cardClass = cardClass;
        this.isInfinite = isInfinite;
        this.series = series;
    }

    public void checkFinitudeConsistency() {
        if (!series.stream().allMatch(serie -> serie.isInfinite() == isInfinite)) {
            throw new CardClassFinitudeMismatch(cardClass);
        }
    }

    @Data
    @Builder
    public static class PickNextCardResult {
        public final Card card;
        public final CardClass remainingCards;
    }

    public PickNextCardResult pickNextCard(long seed) {
        int serieIndex;
        if (isInfinite)
            serieIndex = (int) ((seed & LONG_MASK) % series.size());
        else {
            int total = 0;
            int[] rollingSumOfCount = new int[series.size()];
            for (int i = 0; i < series.size(); i++) {
                total += series.get(i).count();
                rollingSumOfCount[i] = total;
            }
            if (total == 0)
                throw new CardClassEmpty(cardClass);
            int randomCardIndex = (int) ((seed & LONG_MASK) % total);
            serieIndex = 0;
            while (randomCardIndex >= rollingSumOfCount[serieIndex]) {
                serieIndex++;
            }
        }
        final CardSerie serie = series.get(serieIndex);
        final CardSerie.PickNextCardResult pick = serie.pickNextCard();
        return PickNextCardResult.builder()
                .card(new Card(cardClass, serie.name, pick.cardOrderNumber))
                .remainingCards(toBuilder().series(ListUtils.subst(series, serie, pick.remainingCards)).build())
                .build();
    }

    public CardClass addCard(Card card) throws NoSuchCardSerie {
        final CardSerie serieToExtend = getSerieByName(card.serieName);
        final CardSerie extendedSerie = serieToExtend.addCard(card.orderNumber);
        return toBuilder().series(ListUtils.subst(series, serieToExtend, extendedSerie)).build();
    }

    public CardSerie getSerieByName(final String name) throws NoSuchCardSerie {
        return series.stream().filter(serie -> name.equals(serie.name)).findFirst().orElseThrow(() -> new NoSuchCardSerie(name));
    }

    protected Integer count() {
        if (isInfinite)
            return null;
        else {
            int total = 0;
            for (CardSerie serie : series) {
                total += serie.count();
            }
            return total;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CardClass that = (CardClass) o;
        return cardClass.equals(that.cardClass) && isInfinite == that.isInfinite && series.equals(that.series);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardClass, isInfinite, series);
    }
}
