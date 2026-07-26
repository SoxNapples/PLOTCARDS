package net.tpi.tradingcards.item;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.tpi.tradingcards.TPITradingCards;
import net.tpi.tradingcards.block.ModBlocks;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;

/** Registers a dedicated creative-inventory tab holding all trading cards. */
public final class ModItemGroups {

	private ModItemGroups() {
	}

	public static final ResourceKey<CreativeModeTab> TPI_CARDS_KEY =
			ResourceKey.create(Registries.CREATIVE_MODE_TAB, Identifier.fromNamespaceAndPath(TPITradingCards.MOD_ID, "tpi_trading_cards"));

	public static void initialize() {
		Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, TPI_CARDS_KEY, FabricCreativeModeTab.builder()
				.title(Component.translatable("itemGroup.tpi_trading_cards.main"))
				.icon(() -> new ItemStack(ModItems.CARVED_CARD))
				.displayItems((context, output) -> {
					for (Item card : ModItems.ALL_CARDS) {
						output.accept(card);
					}
					output.accept(ModBlocks.MICROPHONE_ITEM);
				})
				.build());
	}
}
