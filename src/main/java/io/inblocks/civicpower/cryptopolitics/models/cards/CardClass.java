package io.inblocks.civicpower.cryptopolitics.models.cards;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.inblocks.civicpower.cryptopolitics.ListUtils;
import io.inblocks.civicpower.cryptopolitics.exceptions.CardClassEmpty;
import io.inblocks.civicpower.cryptopolitics.exceptions.CardClassFinitudeMismatch;
import io.inblocks.civicpower.cryptopolitics.exceptions.NoSuchCardSerie;
import io.inblocks.civicpower.cryptopolitics.models.SerieRetirement;
import io.inblocks.civicpower.cryptopolitics.models.Weighted;
import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@Introspected
@Builder(toBuilder = true)
public class CardClass {
    @NotNull
    public final String cardClass;
    @Valid @NotNull @JsonProperty(value="infinite")
    public final Boolean isInfinite;
    @Valid @NotNull @JsonInclude(JsonInclude.Include.ALWAYS)
    public final List<CardSerie> series;

    static final long LONG_MASK = 0xffffffffL;

    public CardClass(final String cardClass, final Boolean isInfinite, final List<CardSerie> series) {
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
        final List<CardSerie> activeSeries = series.stream().filter(s -> !s.retired).toList();
        if (isInfinite) {
            if (activeSeries.isEmpty())
                throw new CardClassEmpty(cardClass);
            serie = activeSeries.get((int) ((seed & LONG_MASK) % activeSeries.size()));
        } else {
            try {
                serie = Weighted.select(activeSeries.stream().map(s -> new Weighted<>(s, s.count())).toList(), seed);
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
        return toBuilder()
            .series(ListUtils.subst(series, serieToExtend, extendedSerie))
            .build();
    }

    public CardClass retireSeries(List<SerieRetirement> seriesRetirements) {
        Map<String, Boolean> newRetirements = series.stream().collect(Collectors.toMap(serie -> serie.name, serie -> serie.retired));
        for (SerieRetirement serieRetirement : seriesRetirements) {
            if (!newRetirements.containsKey(serieRetirement.serieName()))
                throw new NoSuchCardSerie(serieRetirement.serieName());
            newRetirements.put(serieRetirement.serieName(), serieRetirement.isRetired());
        }
        return toBuilder()
                .series(series.stream()
                            .map(serie -> {
                                final Boolean shouldBeRetired = newRetirements.get(serie.name);
                                return serie.retired != shouldBeRetired ? serie.toBuilder().retired(shouldBeRetired).build() : serie;
                            })
                            .toList()
                        )
                .build();
    }

    public CardSerie getCardSerieByName(final String name) throws NoSuchCardSerie {
        return series.stream().filter(serie -> name.equals(serie.name)).findFirst().orElseThrow(() -> new NoSuchCardSerie(name));
    }

    public Integer count() {
        if (isInfinite)
            return null;
        else {
            int total = 0;
            for (CardSerie serie : series) {
                if (!serie.retired)
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
