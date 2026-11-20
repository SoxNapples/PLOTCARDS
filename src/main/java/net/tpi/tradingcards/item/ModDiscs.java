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
 * Built-in music discs - The Plot in You's discography, one disc per song.
 *
 * Uses vanilla's data-driven jukebox system, so no AudioPlayer / Simple Voice
 * Chat is needed. Per song:
 *   - a SoundEvent registered here (tpi_trading_cards:music_disc.<id>)
 *   - assets/tpi_trading_cards/sounds.json maps it to sounds/records/<id>.ogg
 *   - data/tpi_trading_cards/jukebox_song/<id>.json holds the length, comparator
 *     output and "Now Playing" text
 *   - a disc item with the JUKEBOX_PLAYABLE component
 *
 * Only the 12 self-titled-album discs have recipes (9 matching song cards each);
 * the rest of the discography is found in the item frame at concert stages.
 *
 * The song audio (.ogg) is deliberately NOT in the git repo - it's the band's
 * copyrighted music. It only goes into jars built for private use. Without it
 * the discs still register and spin in a jukebox, just silently.
 */
public final class ModDiscs {

	private ModDiscs() {
	}

	private record Song(String id, String album, int year) {
	}

	private static final String SELF_TITLED = "The Plot in You";

	/** Release order, then track order. */
	private static final Song[] SONGS = {
			new Song("wife_beater", "Wife Beater", 2009),
			new Song("clots", "Wife Beater", 2009),
			new Song("molester", "Wife Beater", 2009),
			new Song("abuser", "Wife Beater", 2009),
			new Song("bleeders", "Wife Beater", 2009),
			new Song("guts", "Wife Beater", 2009),
			new Song("small_face", "Wife Beater", 2009),
			new Song("the_fathers_seed", "First Born", 2011),
			new Song("small_face_first_born", "First Born", 2011),
			new Song("bully", "First Born", 2011),
			new Song("miscarriage", "First Born", 2011),
			new Song("rat_poison", "First Born", 2011),
			new Song("neighbors", "First Born", 2011),
			new Song("filth", "First Born", 2011),
			new Song("unwelcome", "First Born", 2011),
			new Song("nothing_leaves_this_room", "First Born", 2011),
			new Song("dear_dad", "First Born", 2011),
			new Song("premeditated", "Could You Watch Your Children Burn", 2012),
			new Song("fiction_religion", "Could You Watch Your Children Burn", 2012),
			new Song("digging_your_grave", "Could You Watch Your Children Burn", 2012),
			new Song("population_control", "Could You Watch Your Children Burn", 2012),
			new Song("troll", "Could You Watch Your Children Burn", 2012),
			new Song("the_devils_contract", "Could You Watch Your Children Burn", 2012),
			new Song("sober_and_soulless", "Could You Watch Your Children Burn", 2012),
			new Song("bible_butcher", "Could You Watch Your Children Burn", 2012),
			new Song("glad_youre_gone", "Could You Watch Your Children Burn", 2012),
			new Song("hole_in_the_wall", "Happiness in Self Destruction", 2014),
			new Song("dear_old_friend", "Happiness in Self Destruction", 2014),
			new Song("take_me_away", "Happiness in Self Destruction", 2014),
			new Song("runaway", "Happiness in Self Destruction", 2014),
			new Song("pillhead", "Happiness in Self Destruction", 2014),
			new Song("better_vibes", "Happiness in Self Destruction", 2014),
			new Song("my_old_ways", "Happiness in Self Destruction", 2014),
			new Song("die_like_your_brothers", "Happiness in Self Destruction", 2014),
			new Song("mind_controlled", "Happiness in Self Destruction", 2014),
			new Song("time_changes_everything", "Happiness in Self Destruction", 2014),
			new Song("living_your_dream", "Happiness in Self Destruction", 2014),
			new Song("a_song_about_myself", "Happiness in Self Destruction", 2014),
			new Song("forgive_me", "Happiness in Self Destruction", 2014),
			new Song("washed_up", "Happiness in Self Destruction", 2014),
			new Song("happiness_in_self_destruction", "Happiness in Self Destruction", 2014),
			new Song("crows", "Crows", 2016),
			new Song("rigged", "Dispose", 2018),
			new Song("not_just_breathing", "Dispose", 2018),
			new Song("one_last_time", "Dispose", 2018),
			new Song("i_always_wanted_to_leave", "Dispose", 2018),
			new Song("feel_nothing", "Dispose", 2018),
			new Song("happy", "Dispose", 2018),
			new Song("the_one_you_loved", "Dispose", 2018),
			new Song("paid_in_full", "Dispose", 2018),
			new Song("the_sound", "Dispose", 2018),
			new Song("disposable_fix", "Dispose", 2018),
			new Song("letters_to_a_dead_friend", "Swan Song", 2021),
			new Song("fall_again", "Swan Song", 2021),
			new Song("face_me", "Swan Song", 2021),
			new Song("too_far_gone", "Swan Song", 2021),
			new Song("paradigm", "Swan Song", 2021),
			new Song("both_to_blame", "Swan Song", 2021),
			new Song("too_heavy", "Swan Song", 2021),
			new Song("enemy", "Swan Song", 2021),
			new Song("whole_without_me", "Swan Song", 2021),
			new Song("freed", "Swan Song", 2021),
			new Song("divide", "The Plot in You", 2026),
			new Song("left_behind", "The Plot in You", 2026),
			new Song("forgotten", "The Plot in You", 2026),
			new Song("closure", "The Plot in You", 2026),
			new Song("dont_look_away", "The Plot in You", 2026),
			new Song("all_that_i_can_give", "The Plot in You", 2026),
			new Song("been_here_before", "The Plot in You", 2026),
			new Song("pretend", "The Plot in You", 2026),
			new Song("spare_me", "The Plot in You", 2026),
			new Song("silence", "The Plot in You", 2026),
			new Song("you_get_one", "The Plot in You", 2026),
			new Song("carved", "The Plot in You", 2026)
	};

	private static final List<Item> DISCS = new ArrayList<>();

	private static final List<Item> STAGE_LOOT = new ArrayList<>();

	/** Every disc, in release + track order - used by the creative tab. */
	public static final List<Item> ALL_DISCS = Collections.unmodifiableList(DISCS);

	/**
	 * Discs with no crafting recipe (everything outside the self-titled album, whose 12 discs
	 * are pressed from song cards). Concert stages hand these out - see StageDiscHandler.
	 */
	public static final List<Item> STAGE_LOOT_DISCS = Collections.unmodifiableList(STAGE_LOOT);

	static {
		for (Song song : SONGS) {
			Item disc = register(song);
			DISCS.add(disc);
			if (!SELF_TITLED.equals(song.album())) {
				STAGE_LOOT.add(disc);
			}
		}
	}

	private static Item register(Song song) {
		Identifier soundId = Identifier.fromNamespaceAndPath(TPITradingCards.MOD_ID, "music_disc." + song.id());
		Registry.register(BuiltInRegistries.SOUND_EVENT, soundId, SoundEvent.createVariableRangeEvent(soundId));

		ResourceKey<JukeboxSong> songKey = ResourceKey.create(Registries.JUKEBOX_SONG,
				Identifier.fromNamespaceAndPath(TPITradingCards.MOD_ID, song.id()));
		ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM,
				Identifier.fromNamespaceAndPath(TPITradingCards.MOD_ID, song.id() + "_disc"));

		Item disc = new MusicDiscItem(new Item.Properties()
				.setId(itemKey)
				.stacksTo(1)
				.rarity(Rarity.RARE)
				.jukeboxPlayable(songKey), song.album(), song.year());
		return Registry.register(BuiltInRegistries.ITEM, itemKey, disc);
	}

	public static void initialize() {
		TPITradingCards.LOGGER.info("Registered {} music discs", ALL_DISCS.size());
	}
}
