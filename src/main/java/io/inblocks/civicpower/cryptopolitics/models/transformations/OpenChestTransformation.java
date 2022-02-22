package io.inblocks.civicpower.cryptopolitics.models.transformations;

import io.inblocks.civicpower.cryptopolitics.models.*;
import io.inblocks.civicpower.cryptopolitics.models.selections.FromClass;
import io.inblocks.civicpower.cryptopolitics.models.selections.OneOf;
import io.inblocks.civicpower.cryptopolitics.models.selections.Times;
import io.inblocks.civicpower.cryptopolitics.models.selections.Together;
import io.micronaut.core.annotation.Introspected;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Introspected
public class OpenChestTransformation implements Transformation {

    private static final Selection FREE_CARD_CLASS = new FromClass("FREE");
    private static final Selection COMMON_CARD_CLASS = new FromClass("COMMON");
    private static final Selection RARE_CARD_CLASS = new FromClass("RARE");
    private static final Selection EPIC_CARD_CLASS = new FromClass("EPIC");
    private static final Selection LEGENDARY_CARD_CLASS = new FromClass("LEGENDARY");

    private static final Selection FREE_CHEST_CONTENT = new Times(3, FREE_CARD_CLASS);
    private static final Selection COMMON_CHEST_CONTENT = new Together(List.of(
            new Times(2, COMMON_CARD_CLASS),
            new OneOf(List.of(new Weighted<>(COMMON_CARD_CLASS, 2), new Weighted<>(RARE_CARD_CLASS, 1)))));
    private static final Selection RARE_CHEST_CONTENT = new Together(List.of(
            new Times(2, COMMON_CARD_CLASS),
            new OneOf(List.of(new Weighted<>(COMMON_CARD_CLASS, 2), new Weighted<>(RARE_CARD_CLASS, 1))),
            RARE_CARD_CLASS));
    private static final Selection EPIC_CHEST_CONTENT = new Together(List.of(
            new Times(2, COMMON_CARD_CLASS),
            new Times(2, RARE_CARD_CLASS),
            new OneOf(List.of(new Weighted<>(RARE_CARD_CLASS, 4), new Weighted<>(EPIC_CARD_CLASS, 1)))));
    private static final Selection LEGENDARY_CHEST_CONTENT = new Together(List.of(
            new Times(3, COMMON_CARD_CLASS),
            new Times(3, RARE_CARD_CLASS),
            EPIC_CARD_CLASS,
            new OneOf(List.of(new Weighted<>(EPIC_CARD_CLASS, 4), new Weighted<>(LEGENDARY_CARD_CLASS, 1)))));
    private static final Selection ETERNAL_CHEST_CONTENT = new Together(List.of(
            new Times(5, COMMON_CARD_CLASS),
            new Times(5, RARE_CARD_CLASS),
            new Times(2, EPIC_CARD_CLASS),
            new OneOf(List.of(new Weighted<>(EPIC_CARD_CLASS, 2), new Weighted<>(LEGENDARY_CARD_CLASS, 1)))));

    @Valid @NotNull public final ChestType chestType;

  private PickCardsTransformation pickCardsTransformation;

  public OpenChestTransformation(final ChestType chestType) {
      this.chestType = chestType;
  }

  private Selection getCardSelection(ChestType chestType) {
    return
      switch (chestType) {
          case FREE -> FREE_CHEST_CONTENT;
          case COMMON -> COMMON_CHEST_CONTENT;
          case RARE -> RARE_CHEST_CONTENT;
          case EPIC -> EPIC_CHEST_CONTENT;
          case LEGENDARY -> LEGENDARY_CHEST_CONTENT;
          case ETERNAL -> ETERNAL_CHEST_CONTENT;
      };
  }

  @Override
  public Talon apply(final Context context, final Talon in) {
      final Selection selection = getCardSelection(chestType);
      pickCardsTransformation = new PickCardsTransformation(selection);
      return pickCardsTransformation.apply(context, in);
  }

  @Override
  public Object getResults() {
    return pickCardsTransformation == null ? null : pickCardsTransformation.getResults();
  }
}
