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
    public final List<CardSerie> retiredSeries;

    static final long LONG_MASK = 0xffffffffL;

    public CardClass(final String cardClass, final Boolean isInfinite, final List<CardSerie> series, final List<CardSerie> retiredSeries) {
        this.cardClass = cardClass;
        this.isInfinite = isInfinite;
        this.series = series;
        this.retiredSeries = Optional.ofNullable(retiredSeries).orElse(Collections.emptyList());
    }

    public void checkFinitudeConsistency() {
        if (!Stream.of(series, retiredSeries)
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
                final Optional<CardSerie> maybeRetiredSerieToExtend =
                    getOptionalCardSerieByName(card.serieName, retiredSeries);
                return maybeRetiredSerieToExtend
                    .map(
                        retiredSerieToExtend -> {
                          final CardSerie extendedRetiredSerie =
                              retiredSerieToExtend.addCard(card.orderNumber);
                          return toBuilder()
                              .retiredSeries(
                                  ListUtils.subst(
                                      retiredSeries, retiredSerieToExtend, extendedRetiredSerie))
                              .build();
                        })
                    .orElseThrow(() -> new NoSuchCardSerie(card.serieName));
              });
    }

    public CardClass modifyActiveSeries(List<String> seriesToRetire, List<String> seriesToReinstate) {
      final Map<Boolean, List<CardSerie>> activeSeriesPartition = splitSeries(series, seriesToRetire);
      final Map<Boolean, List<CardSerie>> retiredSeriesPartition = splitSeries(retiredSeries, seriesToReinstate);
      return toBuilder()
          .series(
              ListUtils.concat(activeSeriesPartition.get(true), retiredSeriesPartition.get(false)))
          .retiredSeries(
              ListUtils.concat(retiredSeriesPartition.get(true), activeSeriesPartition.get(false)))
          .build();
    }

    private Map<Boolean, List<CardSerie>> splitSeries(final List<CardSerie> series, final List<String> seriesToRemove) {
        Set<String> stayingSeries =
            series.stream().map(serie -> serie.name).collect(Collectors.toSet());
        for (String serieToRemove : seriesToRemove) {
            if (!stayingSeries.remove(serieToRemove)) throw new NoSuchCardSerie(serieToRemove);
        }
        return series.stream()
            .collect(Collectors.partitioningBy(serie -> stayingSeries.contains(serie.name)));
    }

    public CardSerie getCardSerieByName(final String name) throws NoSuchCardSerie {
        return getOptionalCardSerieByName(name, series).orElseThrow(() -> new NoSuchCardSerie(name));
    }

    public CardSerie getRetiredCardSerieByName(final String name) throws NoSuchCardSerie {
        return getOptionalCardSerieByName(name, retiredSeries)
          .orElseThrow(() -> new NoSuchCardSerie(name));
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
        return cardClass.equals(that.cardClass) && isInfinite == that.isInfinite && series.equals(that.series) && retiredSeries.equals(that.retiredSeries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardClass, isInfinite, series, retiredSeries);
    }
}
