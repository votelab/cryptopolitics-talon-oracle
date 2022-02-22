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

import javax.validation.Valid;
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
    @Valid @NotNull
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
    public static class PickCardResult {
        public final Card card;
        public final CardClass remainingCards;
    }

    public PickCardResult pickCard(long seed) {
        final CardSerie serie;
        if (isInfinite) {
            if (series.isEmpty())
                throw new CardClassEmpty(cardClass);
            serie = series.get((int) ((seed & LONG_MASK) % series.size()));
        } else {
            try {
                serie = Weighted.select(series.stream().map(s -> new Weighted<>(s, s.count())).toList(), seed);
            } catch (IllegalArgumentException e) {
                throw new CardClassEmpty(cardClass);
            }
        }
        final CardSerie.PickCardResult pick = serie.pickCard();
        return CardClass.PickCardResult.builder()
                .card(new Card(cardClass, serie.name, pick.cardOrderNumber))
                .remainingCards(toBuilder().series(ListUtils.subst(series, serie, pick.remainingCards)).build())
                .build();
    }

    public CardClass addCard(Card card) throws NoSuchCardSerie {
        final CardSerie serieToExtend = getCardSerieByName(card.serieName);
        final CardSerie extendedSerie = serieToExtend.addCard(card.orderNumber);
        return toBuilder().series(ListUtils.subst(series, serieToExtend, extendedSerie)).build();
    }

    public CardSerie getCardSerieByName(final String name) throws NoSuchCardSerie {
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
