package net.tpi.tradingcards.item;

import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

/**
 * Base item for every "The Plot in You" trading card. A single class handles
 * all cards - what differs between cards is just the data passed into the
 * constructor (see {@link net.tpi.tradingcards.item.ModItems} for the full list).
 *
 * Verified against the real decompiled 26.2 sources (official/Mojang mappings) -
 * appendHoverText's signature below is confirmed correct for this game version.
 */
public class TradingCardItem extends Item {

	/** What kind of subject the card depicts. */
	public enum CardCategory {
		SONG,
		BAND_MEMBER,
		MASCOT
	}

	private final CardCategory category;
	/** Album name (for SONG cards) or instrument/role (for BAND_MEMBER cards). */
	private final String subtitle;
	/** Release year, or 0 if not applicable (e.g. a band member card). */
	private final int year;
	private final CardRarity rarity;

	public TradingCardItem(Properties properties, CardCategory category, String subtitle, int year, CardRarity rarity) {
		super(properties);
		this.category = category;
		this.subtitle = subtitle;
		this.year = year;
		this.rarity = rarity;
	}

	public CardRarity getRarity() {
		return rarity;
	}

	public CardCategory getCategory() {
		return category;
	}

	/** Width/height ratio of this card's front texture - square for song cards, 750x1050 portrait for everything else. */
	private static final float PORTRAIT_ASPECT = 750.0F / 1050.0F;

	public float getAspectRatio() {
		return category == CardCategory.SONG ? 1.0F : PORTRAIT_ASPECT;
	}

	@Override
	public void appendHoverText(
			final ItemStack itemStack, final Item.TooltipContext context, final TooltipDisplay display,
			final Consumer<Component> textConsumer, final TooltipFlag tooltipFlag) {
		textConsumer.accept(rarity.asComponent());

		switch (category) {
			case SONG -> {
				textConsumer.accept(Component.literal("From: " + subtitle));
				textConsumer.accept(Component.literal("Released: " + year));
			}
			case BAND_MEMBER, MASCOT -> textConsumer.accept(Component.literal(subtitle));
		}

		textConsumer.accept(Component.literal("The Plot in You").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
	}
}
