package net.tpi.tradingcards.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.Rarity;
import net.tpi.tradingcards.TPITradingCards;

/**
 * Built-in music discs - one per song card, pressed from nine copies of that card.
 *
 * Uses vanilla's own data-driven jukebox system, so no AudioPlayer / Simple Voice
 * Chat is needed:
 *   - a SoundEvent per song, registered here (tpi_trading_cards:music_disc.<song>)
 *   - assets/tpi_trading_cards/sounds.json maps it to sounds/records/<song>.ogg
 *   - data/tpi_trading_cards/jukebox_song/<song>.json gives the length, comparator
 *     output and the tooltip line, and points at the SoundEvent
 *   - the disc item gets the JUKEBOX_PLAYABLE component via Properties#jukeboxPlayable
 */
public final class ModDiscs {

	private ModDiscs() {
	}

	private static final List<Item> DISCS = new ArrayList<>();

	public static final Item DIVIDE_DISC = register("divide");
	public static final Item LEFT_BEHIND_DISC = register("left_behind");
	public static final Item FORGOTTEN_DISC = register("forgotten");
	public static final Item CLOSURE_DISC = register("closure");
	public static final Item DONT_LOOK_AWAY_DISC = register("dont_look_away");
	public static final Item ALL_THAT_I_CAN_GIVE_DISC = register("all_that_i_can_give");
	public static final Item BEEN_HERE_BEFORE_DISC = register("been_here_before");
	public static final Item PRETEND_DISC = register("pretend");
	public static final Item SPARE_ME_DISC = register("spare_me");
	public static final Item SILENCE_DISC = register("silence");
	public static final Item YOU_GET_ONE_DISC = register("you_get_one");
	public static final Item CARVED_DISC = register("carved");

	/** All 12 discs in album track order - used by the creative tab. */
	public static final List<Item> ALL_DISCS = Collections.unmodifiableList(DISCS);

	private static Item register(String song) {
		Identifier soundId = Identifier.fromNamespaceAndPath(TPITradingCards.MOD_ID, "music_disc." + song);
		Registry.register(BuiltInRegistries.SOUND_EVENT, soundId, SoundEvent.createVariableRangeEvent(soundId));

		ResourceKey<JukeboxSong> songKey = ResourceKey.create(Registries.JUKEBOX_SONG,
				Identifier.fromNamespaceAndPath(TPITradingCards.MOD_ID, song));

		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM,
				Identifier.fromNamespaceAndPath(TPITradingCards.MOD_ID, song + "_disc"));
		Item disc = new Item(new Item.Properties()
				.setId(itemKey)
				.stacksTo(1)
				.rarity(Rarity.RARE)
				.jukeboxPlayable(songKey));
		Registry.register(BuiltInRegistries.ITEM, itemKey, disc);
		DISCS.add(disc);
		return disc;
	}

	public static void initialize() {
		TPITradingCards.LOGGER.info("Registered {} music discs", ALL_DISCS.size());
	}
}
