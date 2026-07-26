package net.tpi.tradingcards.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Rarity tiers for trading cards. Controls the tooltip color and is displayed
 * as its own tooltip line so players can tell rare pulls apart at a glance.
 */
public enum CardRarity {
	COMMON("Common", ChatFormatting.GRAY),
	UNCOMMON("Uncommon", ChatFormatting.GREEN),
	RARE("Rare", ChatFormatting.AQUA),
	LEGENDARY("Legendary", ChatFormatting.GOLD),
	MYTHIC("Mythic", ChatFormatting.LIGHT_PURPLE);

	private final String label;
	private final ChatFormatting color;

	CardRarity(String label, ChatFormatting color) {
		this.label = label;
		this.color = color;
	}

	public ChatFormatting getColor() {
		return color;
	}

	public MutableComponent asComponent() {
		return Component.literal(label).withStyle(color);
	}
}
