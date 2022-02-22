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
import java.util.EnumMap;
import java.util.Map;

@Data
@Introspected
public class OpenChestTransformation implements Transformation {

    enum Locale {
        FR,
    }

    private static Selection freeCardClass(Locale locale) { return new FromClass("FREE_" + locale); }
    private static Selection commonCardClass(Locale locale) { return new FromClass("COMMON_" + locale); }
    private static Selection rareCardClass(Locale locale) { return new FromClass("RARE_" + locale); }
    private static Selection epicCardClass(Locale locale) { return new FromClass("EPIC_" + locale); }
    private static Selection legendaryCardClass(Locale locale) { return new FromClass("LEGENDARY_" + locale); }

    private static Selection freeChestContent(Locale locale) { return new Times(3, freeCardClass(locale)); }
    private static Selection commonChestContent(Locale locale) { return new Together(
            new Times(2, commonCardClass(locale)),
            new OneOf(new Weighted<>(commonCardClass(locale), 2), new Weighted<>(rareCardClass(locale), 1))); }
    private static Selection rareChestContent(Locale locale) { return new Together(
            new Times(2, commonCardClass(locale)),
            new OneOf(new Weighted<>(commonCardClass(locale), 2), new Weighted<>(rareCardClass(locale), 1)),
            rareCardClass(locale)); }
    private static Selection epicChestContent(Locale locale) { return new Together(
            new Times(2, commonCardClass(locale)),
            new Times(2, rareCardClass(locale)),
            new OneOf(new Weighted<>(rareCardClass(locale), 4), new Weighted<>(epicCardClass(locale), 1))); }
    private static Selection legendaryChestContent(Locale locale) { return new Together(
            new Times(3, commonCardClass(locale)),
            new Times(3, rareCardClass(locale)),
            epicCardClass(locale),
            new OneOf(new Weighted<>(epicCardClass(locale), 4), new Weighted<>(legendaryCardClass(locale), 1))); }
    private static Selection eternalChestContent(Locale locale) { return new Together(
            new Times(5, commonCardClass(locale)),
            new Times(5, rareCardClass(locale)),
            new Times(2, epicCardClass(locale)),
            new OneOf(new Weighted<>(epicCardClass(locale), 2), new Weighted<>(legendaryCardClass(locale), 1))); }

    private static Map<ChestType, Selection> buildLocalizedChests(Locale locale) {
        Map<ChestType, Selection> result = new EnumMap<>(ChestType.class);
        result.put(ChestType.FREE, freeChestContent(locale));
        result.put(ChestType.COMMON, commonChestContent(locale));
        result.put(ChestType.RARE, rareChestContent(locale));
        result.put(ChestType.EPIC, epicChestContent(locale));
        result.put(ChestType.LEGENDARY, legendaryChestContent(locale));
        result.put(ChestType.ETERNAL, eternalChestContent(locale));
        return result;
    }

    private static Map<ChestType, Selection> frenchChests = buildLocalizedChests(Locale.FR);

    @Valid @NotNull public final ChestType chestType;

  private PickCardsTransformation pickCardsTransformation;

  public OpenChestTransformation(final ChestType chestType) {
      this.chestType = chestType;
  }

  private Selection getCardSelection(ChestType chestType) {
    return
      switch (chestType) {
          case FREE -> frenchChests.get(ChestType.FREE);
          case COMMON -> frenchChests.get(ChestType.COMMON);
          case RARE -> frenchChests.get(ChestType.RARE);
          case EPIC -> frenchChests.get(ChestType.EPIC);
          case LEGENDARY -> frenchChests.get(ChestType.LEGENDARY);
          case ETERNAL -> frenchChests.get(ChestType.ETERNAL);
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
