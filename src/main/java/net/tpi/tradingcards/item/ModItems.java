package net.tpi.tradingcards.item;

import java.util.List;
import java.util.function.Function;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.tpi.tradingcards.TPITradingCards;
import net.tpi.tradingcards.item.TradingCardItem.CardCategory;

/**
 * Registers every trading card item.
 *
 * Song data is drawn from The Plot in You's self-titled album (2026, Fearless
 * Records) - a 12-track collection unifying the band's "Divide" era rollout:
 * standalone singles Divide/Left Behind/Forgotten (2022-2023), then the
 * three-track Vol. 2/Vol. 3 EPs (2024), the standalone "Silence" single
 * (2025), and finally "You Get One" and "Carved" as the album's own new 2026
 * material. Each card's stored year is that SONG's real original release,
 * not the compilation album's 2026 date - verified against real sources
 * (Wikipedia, Fearless Records, contemporary music press) per song, not
 * assumed from the album date. Plus the band's public touring lineup and the
 * Sox mythic card. Rarity is weighted toward the album's closing singles (its
 * newest, freshest material) rather than the opening one. Double check all of
 * this against the band's official channels if you add more cards later.
 */
public final class ModItems {

	private ModItems() {
	}

	// --- Song cards: self-titled album (2026) - year is each song's real original release (single/EP), not the album's ---
	public static final Item DIVIDE_CARD = register("divide_card",
			properties -> new TradingCardItem(properties, CardCategory.SONG, "The Plot in You", 2022, CardRarity.LEGENDARY));

	public static final Item LEFT_BEHIND_CARD = register("left_behind_card",
			properties -> new TradingCardItem(properties, CardCategory.SONG, "The Plot in You", 2023, CardRarity.UNCOMMON));

	public static final Item FORGOTTEN_CARD = register("forgotten_card",
			properties -> new TradingCardItem(properties, CardCategory.SONG, "The Plot in You", 2023, CardRarity.COMMON));

	public static final Item CLOSURE_CARD = register("closure_card",
			properties -> new TradingCardItem(properties, CardCategory.SONG, "The Plot in You", 2024, CardRarity.UNCOMMON));

	public static final Item DONT_LOOK_AWAY_CARD = register("dont_look_away_card",
			properties -> new TradingCardItem(properties, CardCategory.SONG, "The Plot in You", 2024, CardRarity.RARE));

	public static final Item BEEN_HERE_BEFORE_CARD = register("been_here_before_card",
			properties -> new TradingCardItem(properties, CardCategory.SONG, "The Plot in You", 2024, CardRarity.COMMON));

	public static final Item PRETEND_CARD = register("pretend_card",
			properties -> new TradingCardItem(properties, CardCategory.SONG, "The Plot in You", 2024, CardRarity.RARE));

	public static final Item ALL_THAT_I_CAN_GIVE_CARD = register("all_that_i_can_give_card",
			properties -> new TradingCardItem(properties, CardCategory.SONG, "The Plot in You", 2024, CardRarity.COMMON));

	public static final Item SPARE_ME_CARD = register("spare_me_card",
			properties -> new TradingCardItem(properties, CardCategory.SONG, "The Plot in You", 2024, CardRarity.LEGENDARY));

	public static final Item SILENCE_CARD = register("silence_card",
			properties -> new TradingCardItem(properties, CardCategory.SONG, "The Plot in You", 2025, CardRarity.RARE));

	public static final Item YOU_GET_ONE_CARD = register("you_get_one_card",
			properties -> new TradingCardItem(properties, CardCategory.SONG, "The Plot in You", 2026, CardRarity.UNCOMMON));

	public static final Item CARVED_CARD = register("carved_card",
			properties -> new TradingCardItem(properties, CardCategory.SONG, "The Plot in You", 2026, CardRarity.LEGENDARY));

	// --- Band member cards ---
	public static final Item LANDON_TEWERS_CARD = register("landon_tewers_card",
			properties -> new TradingCardItem(properties, CardCategory.BAND_MEMBER, "Vocals / Programming", 0, CardRarity.MYTHIC));

	public static final Item JOSH_CHILDRESS_CARD = register("josh_childress_card",
			properties -> new TradingCardItem(properties, CardCategory.BAND_MEMBER, "Guitar", 0, CardRarity.LEGENDARY));

	public static final Item ETHAN_YODER_CARD = register("ethan_yoder_card",
			properties -> new TradingCardItem(properties, CardCategory.BAND_MEMBER, "Bass", 0, CardRarity.LEGENDARY));

	public static final Item MICHAEL_COOPER_CARD = register("michael_cooper_card",
			properties -> new TradingCardItem(properties, CardCategory.BAND_MEMBER, "Drums", 0, CardRarity.LEGENDARY));

	// --- Mythic cards: share one 0.001% roll (and one guaranteed Ender Dragon drop) between them. See CardDropHandler. ---
	public static final Item SOX_CARD = register("sox_card",
			properties -> new TradingCardItem(properties, CardCategory.MASCOT, "Captain of the 7 Seas", 0, CardRarity.MYTHIC));

	/** All cards, used by the creative tab. CardDropHandler filters MYTHIC out of its normal weighted drop pool itself. */
	public static final List<Item> ALL_CARDS = List.of(
			DIVIDE_CARD, LEFT_BEHIND_CARD, FORGOTTEN_CARD, CLOSURE_CARD, DONT_LOOK_AWAY_CARD,
			BEEN_HERE_BEFORE_CARD, PRETEND_CARD, ALL_THAT_I_CAN_GIVE_CARD, SPARE_ME_CARD, SILENCE_CARD,
			YOU_GET_ONE_CARD, CARVED_CARD,
			LANDON_TEWERS_CARD, JOSH_CHILDRESS_CARD, ETHAN_YODER_CARD, MICHAEL_COOPER_CARD,
			SOX_CARD
	);

	private static Item register(String path, Function<Item.Properties, Item> factory) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(TPITradingCards.MOD_ID, path));
		Item item = factory.apply(new Item.Properties().setId(key));
		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	public static void initialize() {
		// Referencing the class is enough to trigger the static field registration above.
		TPITradingCards.LOGGER.info("Registered {} trading cards", ALL_CARDS.size());
	}
}
