package net.tpi.tradingcards;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.tpi.tradingcards.block.ModBlocks;
import net.tpi.tradingcards.buff.CardBuffHandler;
import net.tpi.tradingcards.config.TPIConfig;
import net.tpi.tradingcards.item.ModItemGroups;
import net.tpi.tradingcards.item.ModItems;
import net.tpi.tradingcards.loot.CardDropHandler;

/** Mod entrypoint. Runs on both client and dedicated server (see "environment": "*" in fabric.mod.json). */
public class TPITradingCards implements ModInitializer {

	public static final String MOD_ID = "tpi_trading_cards";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		TPIConfig.load();

		ModBlocks.initialize();
		ModItems.initialize();
		ModItemGroups.initialize();
		CardDropHandler.initialize();
		CardBuffHandler.initialize();

		LOGGER.info("The Plot in You Trading Cards loaded (mythic card drop chance: {}%)",
				TPIConfig.INSTANCE.mythicCardDropChance * 100);
	}
}
