package io.inblocks.civicpower.cryptopolitics.models.cards;

import io.inblocks.civicpower.cryptopolitics.ListUtils;
import io.inblocks.civicpower.cryptopolitics.exceptions.NoSuchCardClass;
import io.inblocks.civicpower.cryptopolitics.models.SelectionResult;
import io.inblocks.civicpower.cryptopolitics.models.SerieRetirement;
import io.inblocks.civicpower.cryptopolitics.models.SeriesRetirementsByClass;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.flatMapping;
import static java.util.stream.Collectors.toList;

@Data
@AllArgsConstructor
@Introspected
@Builder(toBuilder = true)
public class Talon {
  @Valid @NotNull public final List<CardClass> classes;

  public void checkFinitudeConsistency() {
    classes.forEach(CardClass::checkFinitudeConsistency);
  }

  public SelectionResult pickCardByClass(String classToPickFrom, long seed) {
    final CardClass cardClass = getCardClassByName(classToPickFrom);
    final CardClass.PickCardResult pick = cardClass.pickCard(seed);
    return SelectionResult.builder()
        .cards(List.of(pick.card))
        .remainingCards(
            toBuilder()
                .classes(ListUtils.subst(classes, cardClass, pick.remainingCards))
                .build())
        .build();
  }

  public SelectionResult pickCardBySerie(String classToPickFrom, String serieToPickFrom) {
    final CardClass cardClass = getCardClassByName(classToPickFrom);
    final CardSerie cardSerie = cardClass.getCardSerieByName(serieToPickFrom);
    final CardSerie.PickCardResult pick = cardSerie.pickCard();
    return SelectionResult.builder()
            .cards(List.of(new Card(classToPickFrom, serieToPickFrom, pick.cardOrderNumber)))
            .remainingCards(toBuilder()
                    .classes(ListUtils.subst(classes, cardClass, cardClass.toBuilder()
                                    .series(ListUtils.subst(cardClass.series, cardSerie, pick.remainingCards))
                            .build()))
                    .build())
            .build();
  }

  public SelectionResult pickSpecificCard(String classToPickFrom, String serieToPickFrom, int orderNumber) {
    final CardClass cardClass = getCardClassByName(classToPickFrom);
    final CardSerie cardSerie = cardClass.getCardSerieByName(serieToPickFrom);
    final CardSerie remainingCards = cardSerie.removeCard(orderNumber);
    return SelectionResult.builder()
            .cards(List.of(new Card(classToPickFrom, serieToPickFrom, orderNumber)))
            .remainingCards(toBuilder()
                    .classes(ListUtils.subst(classes, cardClass, cardClass.toBuilder()
                                    .series(ListUtils.subst(cardClass.series, cardSerie, remainingCards))
                            .build()))
                    .build())
            .build();
  }

  public Talon addCard(Card card) {
    final CardClass cardClassToExtend = getCardClassByName(card.originalClass);
    final CardClass extendedCardClass = cardClassToExtend.addCard(card);
    return toBuilder()
        .classes(ListUtils.subst(classes, cardClassToExtend, extendedCardClass))
        .build();
  }

  public CardClass getCardClassByName(String classToPickFrom) {
    return classes.stream()
        .filter(cls -> cls.cardClass.equals(classToPickFrom))
        .findFirst()
        .orElseThrow(() -> new NoSuchCardClass(classToPickFrom));
  }

  public Talon retireSeries(final List<SeriesRetirementsByClass> seriesToRetire) {
    final Map<String, List<SerieRetirement>> serieRetirementsByClass = toSerieRetirementsByClass(seriesToRetire);
    return toBuilder()
        .classes(
            classes.stream()
                .map(
                    cardClass -> cardClass.retireSeries(
                            Optional.ofNullable(serieRetirementsByClass.get(cardClass.cardClass)).orElse(Collections.emptyList()))
                )
                .toList())
        .build();
  }

  private Map<String, List<SerieRetirement>> toSerieRetirementsByClass(final List<SeriesRetirementsByClass> seriesToRetire) {
    return seriesToRetire == null || seriesToRetire.isEmpty() ? Collections.emptyMap() :
            seriesToRetire.stream()
            .collect(Collectors.groupingBy(
                    // just for parameter validation
                    seriesRetirementsByClass -> getCardClassByName(seriesRetirementsByClass.cardClass()).cardClass,
                    flatMapping(seriesRetirementsByClass -> seriesRetirementsByClass.cardSeries().stream(), toList())));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Talon talon = (Talon) o;
    return classes.equals(talon.classes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(classes);
  }
}
