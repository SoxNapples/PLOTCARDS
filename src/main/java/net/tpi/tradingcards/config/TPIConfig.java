package net.tpi.tradingcards.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;
import net.tpi.tradingcards.TPITradingCards;

/**
 * Plain JSON config, written to config/tpi_trading_cards.json on both the
 * client and dedicated server. Drives the mob-drop chances without needing a
 * rebuild - server owners can tune it per-world.
 */
public final class TPIConfig {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("tpi_trading_cards.json");

	public static TPIConfig INSTANCE = new TPIConfig();

	/*
	 * Each card rolls independently on every hostile mob kill (so, rarely, more
	 * than one could drop from the same kill). All values are 0.0-1.0, and are
	 * per-card, not per-tier - divide a target "any card in this tier" chance
	 * by the number of cards in that tier to get the per-card number.
	 *
	 * Current tier sizes: common 3, uncommon 3, rare 3, legendary 6 (band
	 * members included). Tuned so any card in the tier drops roughly every:
	 * common 1 in 20, uncommon 1 in 40, rare 1 in 50, legendary 1 in 75 kills.
	 */
	public double commonCardDropChance = 0.0166667;
	public double uncommonCardDropChance = 0.0083333;
	public double rareCardDropChance = 0.0066667;
	public double legendaryCardDropChance = 0.0022222;

	/** Chance (0.0-1.0) that a killed hostile mob drops the Sox mythic card. Default 0.001%. The Ender Dragon always drops it, regardless of this setting. */
	public double mythicCardDropChance = 0.00001;

	/*
	 * Separate, much bigger chance (0.0-1.0) to get one random RARE or
	 * LEGENDARY card specifically from these three notable mobs, on top of
	 * the tiny normal rare/legendary rolls every hostile mob gets above. This
	 * is how rare/legendary cards are meant to actually show up in practice,
	 * without needing to bump the base chance for every common zombie/skeleton.
	 */
	public double endermanRareOrLegendaryBonusChance = 0.015;
	public double wardenRareOrLegendaryBonusChance = 0.10;
	public double witherRareOrLegendaryBonusChance = 0.50;

	/** If true, only mobs killed by a player count towards drops (ignores fire/fall/etc. deaths). */
	public boolean requirePlayerKill = true;

	/**
	 * Relative chance increase per level of Looting on the killer's gear, applied
	 * to every card chance above (including the mythic roll). Default 0.5 means
	 * Looting III gives 1 + 3*0.5 = 2.5x the base chance. Set to 0 to disable.
	 */
	public double lootingBonusPerLevel = 0.5;

	public static void load() {
		if (Files.exists(CONFIG_PATH)) {
			try (Reader reader = Files.newBufferedReader(CONFIG_PATH, StandardCharsets.UTF_8)) {
				INSTANCE = GSON.fromJson(reader, TPIConfig.class);
				if (INSTANCE == null) {
					INSTANCE = new TPIConfig();
				}
			} catch (IOException e) {
				TPITradingCards.LOGGER.error("Failed to read tpi_trading_cards.json, using defaults", e);
				INSTANCE = new TPIConfig();
			}
		}
		// Always (re)write the file so new config fields show up for existing installs.
		save();
	}

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH, StandardCharsets.UTF_8)) {
				GSON.toJson(INSTANCE, writer);
			}
		} catch (IOException e) {
			TPITradingCards.LOGGER.error("Failed to write tpi_trading_cards.json", e);
		}
	}
}
