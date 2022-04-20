package io.inblocks.civicpower.cryptopolitics.models.cards;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.inblocks.civicpower.cryptopolitics.ListUtils;
import io.inblocks.civicpower.cryptopolitics.exceptions.CardClassEmpty;
import io.inblocks.civicpower.cryptopolitics.exceptions.CardClassFinitudeMismatch;
import io.inblocks.civicpower.cryptopolitics.exceptions.NoSuchCardSerie;
import io.inblocks.civicpower.cryptopolitics.models.Weighted;
import io.micronaut.core.annotation.Introspected;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @Valid @NotNull
    public final List<CardSerie> deprecatedSeries;

    static final long LONG_MASK = 0xffffffffL;

    public CardClass(final String cardClass, final Boolean isInfinite, final List<CardSerie> series, final List<CardSerie> deprecatedSeries) {
        this.cardClass = cardClass;
        this.isInfinite = isInfinite;
        this.series = series;
        this.deprecatedSeries = Optional.ofNullable(deprecatedSeries).orElse(Collections.emptyList());
    }

    public void checkFinitudeConsistency() {
    if (!Stream.of(series, deprecatedSeries)
        .flatMap(Collection::stream)
        .allMatch(serie -> serie.isInfinite() == isInfinite)) {
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
      return getOptionalCardSerieByName(card.serieName, series)
          .map(
              serieToExtend -> {
                final CardSerie extendedSerie = serieToExtend.addCard(card.orderNumber);
                return toBuilder()
                    .series(ListUtils.subst(series, serieToExtend, extendedSerie))
                    .build();
              })
          .orElseGet(
              () -> {
                final Optional<CardSerie> maybeDeprecatedSerieToExtend =
                    getOptionalCardSerieByName(card.serieName, deprecatedSeries);
                return maybeDeprecatedSerieToExtend
                    .map(
                        deprecatedSerieToExtend -> {
                          final CardSerie extendedDeprecatedSerie =
                              deprecatedSerieToExtend.addCard(card.orderNumber);
                          return toBuilder()
                              .deprecatedSeries(
                                  ListUtils.subst(
                                      deprecatedSeries,
                                      deprecatedSerieToExtend,
                                      extendedDeprecatedSerie))
                              .build();
                        })
                    .orElseThrow(() -> new NoSuchCardSerie(card.serieName));
              });
    }

    public CardClass deprecateSeriesByName(List<String> seriesToDeprecate) {
        Set<String> seriesToKeep = series.stream().map(serie -> serie.name).collect(Collectors.toSet());
        for (String serieToDeprecate : seriesToDeprecate) {
            if (!seriesToKeep.remove(serieToDeprecate))
                throw new NoSuchCardSerie(serieToDeprecate);
        }
        final Map<Boolean, List<CardSerie>> seriesPartition = series.stream().collect(Collectors.partitioningBy(serie -> seriesToKeep.contains(serie.name)));
        final List<CardSerie> newDeprecatedSeries = new ArrayList<>(deprecatedSeries);
        newDeprecatedSeries.addAll(seriesPartition.get(false));
        return toBuilder().series(seriesPartition.get(true)).deprecatedSeries(newDeprecatedSeries).build();
    }

    public CardSerie getCardSerieByName(final String name) throws NoSuchCardSerie {
        return getOptionalCardSerieByName(name, series).orElseThrow(() -> new NoSuchCardSerie(name));
    }

    public CardSerie getDeprecatedCardSerieByName(final String name) throws NoSuchCardSerie {
        return getOptionalCardSerieByName(name, deprecatedSeries).orElseThrow(() -> new NoSuchCardSerie(name));
    }

    private Optional<CardSerie> getOptionalCardSerieByName(final String name, final List<CardSerie> series) {
        return series.stream().filter(serie -> name.equals(serie.name)).findFirst();
    }

    public Integer count() {
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
        return cardClass.equals(that.cardClass) && isInfinite == that.isInfinite && series.equals(that.series) && deprecatedSeries.equals(that.deprecatedSeries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardClass, isInfinite, series, deprecatedSeries);
    }
}
