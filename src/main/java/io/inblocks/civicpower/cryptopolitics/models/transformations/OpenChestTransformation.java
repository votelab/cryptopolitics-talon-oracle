package io.inblocks.civicpower.cryptopolitics.models.transformations;

import io.inblocks.civicpower.cryptopolitics.models.ChestType;
import io.inblocks.civicpower.cryptopolitics.models.Context;
import io.inblocks.civicpower.cryptopolitics.models.Talon;
import io.inblocks.civicpower.cryptopolitics.models.Transformation;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
@Introspected
public class OpenChestTransformation implements Transformation {

  private static final String FREE_CARD_CLASS = "FREE";
  private static final String COMMON_CARD_CLASS = "COMMON";
  private static final String RARE_CARD_CLASS = "RARE";
  private static final String EPIC_CARD_CLASS = "EPIC";
  private static final String LEGENDARY_CARD_CLASS = "LEGENDARY";

  @Valid @NotNull public final ChestType chestType;

  private PickCardsTransformation pickCardsTransformation;

  public OpenChestTransformation(final ChestType chestType) {
      this.chestType = chestType;
  }

  private List<String> getCardClasses(ChestType chestType, Random r) {
    final List<String> cardClasses = new ArrayList<>();
      switch (chestType) {
          case FREE -> {
              cardClasses.add(FREE_CARD_CLASS);
              cardClasses.add(FREE_CARD_CLASS);
              cardClasses.add(FREE_CARD_CLASS);
          }
          case COMMON -> {
              cardClasses.add(COMMON_CARD_CLASS);
              cardClasses.add(COMMON_CARD_CLASS);
              cardClasses.add(r.nextInt(3) < 2 ? COMMON_CARD_CLASS : RARE_CARD_CLASS);
          }
          case RARE -> {
              cardClasses.add(COMMON_CARD_CLASS);
              cardClasses.add(COMMON_CARD_CLASS);
              cardClasses.add(r.nextInt(3) < 2 ? COMMON_CARD_CLASS : RARE_CARD_CLASS);
              cardClasses.add(RARE_CARD_CLASS);
          }
          case EPIC -> {
              cardClasses.add(COMMON_CARD_CLASS);
              cardClasses.add(COMMON_CARD_CLASS);
              cardClasses.add(RARE_CARD_CLASS);
              cardClasses.add(RARE_CARD_CLASS);
              cardClasses.add(r.nextInt(5) < 4 ? RARE_CARD_CLASS : EPIC_CARD_CLASS);
          }
          case LEGENDARY -> {
              cardClasses.add(COMMON_CARD_CLASS);
              cardClasses.add(COMMON_CARD_CLASS);
              cardClasses.add(COMMON_CARD_CLASS);
              cardClasses.add(RARE_CARD_CLASS);
              cardClasses.add(RARE_CARD_CLASS);
              cardClasses.add(RARE_CARD_CLASS);
              cardClasses.add(EPIC_CARD_CLASS);
              cardClasses.add(r.nextInt(5) < 4 ? EPIC_CARD_CLASS : LEGENDARY_CARD_CLASS);
          }
          case ETERNAL -> {
              cardClasses.add(COMMON_CARD_CLASS);
              cardClasses.add(COMMON_CARD_CLASS);
              cardClasses.add(COMMON_CARD_CLASS);
              cardClasses.add(COMMON_CARD_CLASS);
              cardClasses.add(COMMON_CARD_CLASS);
              cardClasses.add(RARE_CARD_CLASS);
              cardClasses.add(RARE_CARD_CLASS);
              cardClasses.add(RARE_CARD_CLASS);
              cardClasses.add(RARE_CARD_CLASS);
              cardClasses.add(RARE_CARD_CLASS);
              cardClasses.add(EPIC_CARD_CLASS);
              cardClasses.add(EPIC_CARD_CLASS);
              cardClasses.add(r.nextInt(3) < 2 ? EPIC_CARD_CLASS : LEGENDARY_CARD_CLASS);
          }
      }
    return cardClasses;
  }

  @Override
  public Talon apply(final Context context, final Talon in) {
      final List<String> cardClasses = getCardClasses(chestType, context.getRandom());
      pickCardsTransformation = new PickCardsTransformation(cardClasses);
      return pickCardsTransformation.apply(context, in);
  }

  @Override
  public Object getResults() {
    return pickCardsTransformation == null ? null : pickCardsTransformation.getResults();
  }
}
