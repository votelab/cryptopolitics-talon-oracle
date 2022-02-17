package io.inblocks.civicpower.cryptopolitics.models;

import io.inblocks.civicpower.cryptopolitics.ListUtils;
import io.inblocks.civicpower.cryptopolitics.exceptions.NoSuchCardClass;
import io.micronaut.core.annotation.Introspected;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@Introspected
@Builder(toBuilder = true)
public class Talon {
  @NotNull public final List<CardClass> classes;

  public void checkFinitudeConsistency() {
    classes.forEach(CardClass::checkFinitudeConsistency);
  }

  public PickNextCardResult pickNextCard(String classToPickFrom, long seed) {
    CardClass cardClass = getCardClassDataByClass(classToPickFrom);
    CardClass.PickNextCardResult pick = cardClass.pickNextCard(seed);
    return PickNextCardResult.builder()
        .card(pick.card)
        .remainingCards(
            toBuilder()
                .classes(ListUtils.subst(classes, cardClass, pick.remainingCards))
                .build())
        .build();
  }

  public Talon addCard(Card card) {
    final CardClass cardClassToExtend = getCardClassDataByClass(card.originalClass);
    final CardClass extendedCardClass = cardClassToExtend.addCard(card);
    return toBuilder()
        .classes(ListUtils.subst(classes, cardClassToExtend, extendedCardClass))
        .build();
  }

  public CardClass getCardClassDataByClass(String classToPickFrom) {
    return classes.stream()
        .filter(cls -> cls.cardClass.equals(classToPickFrom))
        .findFirst()
        .orElseThrow(() -> new NoSuchCardClass(classToPickFrom));
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
