package io.inblocks.civicpower.cryptopolitics.models.transformations;

import io.inblocks.civicpower.cryptopolitics.exceptions.NoSuchCardClass;
import io.inblocks.civicpower.cryptopolitics.exceptions.NoSuchCardSerie;
import io.inblocks.civicpower.cryptopolitics.models.*;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;

import java.math.BigInteger;
import java.util.stream.Stream;

@Data
@Introspected
public class ExtendTalonTransformation implements Transformation {
  public final Talon additionalCards;

  public ExtendTalonTransformation(final Talon additionalCards) {
    this.additionalCards = additionalCards;
  }

  @Override
  public Talon apply(final Context context, final Talon in) {
    return mergeTalon(in, additionalCards);
  }

  private Talon mergeTalon(Talon in, Talon additionalCards) {
    return Talon.builder()
        .classes(
            Stream.concat(mergeClasses(in, additionalCards), addNewClasses(in, additionalCards))
                .toList())
        .build();
  }

  private Stream<CardClass> mergeClasses(Talon in, Talon additionalCards) {
    return in.classes.stream()
        .map(
            cardClassData -> {
              CardClass extra;
              try {
                extra = additionalCards.getCardClassDataByClass(cardClassData.cardClass);
              } catch (NoSuchCardClass e) {
                return cardClassData;
              }
              return cardClassData.toBuilder()
                  .series(
                      Stream.concat(
                              mergeSeries(cardClassData, extra), addNewSeries(cardClassData, extra))
                          .toList())
                  .build();
            });
  }

  private Stream<CardSerie> mergeSeries(CardClass cardClass, CardClass extraCardClass) {
    return cardClass.series.stream()
        .map(
            cardSerieData -> {
              CardSerie extraSerie;
              try {
                extraSerie = extraCardClass.getSerieByName(cardSerieData.name);
              } catch (NoSuchCardSerie e) {
                return cardSerieData;
              }
              if (extraSerie.count() != extraSerie.size || extraSerie.initialDealIndex != 0)
                throw new IllegalArgumentException("Additional talon should be totally unused");
              final int newSize = cardSerieData.size + extraSerie.size;
              final BigInteger newSetBitmap =
                  cardSerieData.setBitmap.or(extraSerie.setBitmap.shiftLeft(cardSerieData.size));
              return cardSerieData.toBuilder().size(newSize).setBitmap(newSetBitmap).build();
            });
  }

  private Stream<CardSerie> addNewSeries(CardClass cardClass, CardClass extra) {
    return extra.series.stream()
        .flatMap(
            extraCardSerieData -> {
              try {
                cardClass.getSerieByName(extraCardSerieData.name);
              } catch (NoSuchCardSerie e) {
                return Stream.of(extraCardSerieData);
              }
              return Stream.empty();
            });
  }

  private Stream<CardClass> addNewClasses(Talon in, Talon additionalCards) {
    return additionalCards.classes.stream()
        .flatMap(
            extraCardClassData -> {
              try {
                in.getCardClassDataByClass(extraCardClassData.cardClass);
              } catch (NoSuchCardClass e) {
                return Stream.of(extraCardClassData);
              }
              return Stream.empty();
            });
  }
}
