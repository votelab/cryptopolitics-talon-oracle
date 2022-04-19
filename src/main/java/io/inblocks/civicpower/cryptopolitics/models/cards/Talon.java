package io.inblocks.civicpower.cryptopolitics.models.cards;

import io.inblocks.civicpower.cryptopolitics.ListUtils;
import io.inblocks.civicpower.cryptopolitics.exceptions.NoSuchCardClass;
import io.inblocks.civicpower.cryptopolitics.models.SelectionResult;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

  public Talon deprecateSeries(final Map<String, List<String>> seriesToDeprecate) {
    return toBuilder()
            .classes(classes.stream()
                    .map(cardClass -> seriesToDeprecate.containsKey(cardClass.cardClass) ? cardClass.deprecateSeriesByName(seriesToDeprecate.get(cardClass.cardClass)) : cardClass)
                    .toList())
            .build();
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
