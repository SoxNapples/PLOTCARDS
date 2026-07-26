package net.tpi.tradingcards.buff;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.tpi.tradingcards.item.CardRarity;
import net.tpi.tradingcards.item.TradingCardItem;
import net.tpi.tradingcards.item.TradingCardItem.CardCategory;

/**
 * Cards held in the offhand grant passive buffs.
 *
 * Song cards grant an effect by rarity tier: Common - Speed I, Uncommon -
 * Haste I, Rare - Strength I, Legendary - Speed II + Resistance I.
 *
 * Band member ("artist") cards all grant Night Vision + Health Boost I (2
 * hearts), regardless of their own rarity, plus whatever tier effect matches
 * the single highest-rarity song card the player currently owns anywhere in
 * their inventory (not necessarily held). Landon Tewers additionally grants
 * Hero of the Village I on top of that same baseline.
 *
 * Sox (the mascot card) is fully special-cased: Dolphin's Grace I, Jump Boost
 * I, Speed II, and Absorption II (4 hearts).
 */
public final class CardBuffs {

	private CardBuffs() {
	}

	/**
	 * Reapplied every CardBuffHandler tick (every 10 ticks / 0.5s). Vanilla's HUD blinks an effect icon once its
	 * remaining duration drops to <=200 ticks (10s) - confirmed in Hud#renderEffects, which calls
	 * instance.endsWithin(200). 220 ticks (11s) stays just outside that window between refreshes, so it never
	 * visibly blinks (a shorter duration, even 5s, was still always inside the 10s window and blinked constantly).
	 */
	private static final int EFFECT_DURATION_TICKS = 220;
	private static final int AMPLIFIER_I = 0;
	private static final int AMPLIFIER_II = 1;

	public static void tick(ServerPlayer player) {
		ItemStack offhand = player.getOffhandItem();
		if (!(offhand.getItem() instanceof TradingCardItem card)) {
			return;
		}

		switch (card.getCategory()) {
			case MASCOT -> applySoxKit(player);
			case BAND_MEMBER -> applyArtistKit(player, card.getRarity() == CardRarity.MYTHIC);
			case SONG -> applySongTierEffect(player, card.getRarity());
		}
	}

	private static void applySoxKit(ServerPlayer player) {
		apply(player, MobEffects.DOLPHINS_GRACE, AMPLIFIER_I);
		apply(player, MobEffects.JUMP_BOOST, AMPLIFIER_I);
		apply(player, MobEffects.SPEED, AMPLIFIER_II);
		apply(player, MobEffects.ABSORPTION, AMPLIFIER_II);
	}

	private static void applyArtistKit(ServerPlayer player, boolean isLandon) {
		apply(player, MobEffects.NIGHT_VISION, AMPLIFIER_I);
		apply(player, MobEffects.HEALTH_BOOST, AMPLIFIER_I);
		if (isLandon) {
			apply(player, MobEffects.HERO_OF_THE_VILLAGE, AMPLIFIER_I);
		}

		CardRarity bestOwnedSong = highestOwnedSongRarity(player);
		if (bestOwnedSong != null) {
			applySongTierEffect(player, bestOwnedSong);
		}
	}

	/** Highest rarity among SONG cards anywhere in the player's inventory (not necessarily held), or null if they own none. */
	private static CardRarity highestOwnedSongRarity(Player player) {
		CardRarity best = null;
		for (ItemStack stack : player.getInventory().getNonEquipmentItems()) {
			if (stack.getItem() instanceof TradingCardItem card && card.getCategory() == CardCategory.SONG) {
				CardRarity rarity = card.getRarity();
				if (best == null || rarity.ordinal() > best.ordinal()) {
					best = rarity;
				}
			}
		}
		return best;
	}

	private static void applySongTierEffect(ServerPlayer player, CardRarity rarity) {
		switch (rarity) {
			case COMMON -> apply(player, MobEffects.SPEED, AMPLIFIER_I);
			case UNCOMMON -> apply(player, MobEffects.HASTE, AMPLIFIER_I);
			case RARE -> apply(player, MobEffects.STRENGTH, AMPLIFIER_I);
			case LEGENDARY -> {
				apply(player, MobEffects.SPEED, AMPLIFIER_II);
				apply(player, MobEffects.RESISTANCE, AMPLIFIER_I);
			}
			default -> {
				// MYTHIC: no song card is currently Mythic - nothing defined for that case.
			}
		}
	}

	private static void apply(ServerPlayer player, Holder<MobEffect> effect, int amplifier) {
		player.addEffect(new MobEffectInstance(effect, EFFECT_DURATION_TICKS, amplifier, false, false, true));
	}
}
