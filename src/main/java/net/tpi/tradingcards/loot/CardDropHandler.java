package net.tpi.tradingcards.loot;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.tpi.tradingcards.config.TPIConfig;
import net.tpi.tradingcards.item.CardRarity;
import net.tpi.tradingcards.item.ModItems;
import net.tpi.tradingcards.item.TradingCardItem;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;

/**
 * Hooks every hostile-mob death on the server. Every card rolls independently
 * per kill (see {@link TPIConfig}) - rarer tiers get a lower chance, so it's
 * possible (rarely) for more than one card to drop from the same kill. All
 * MYTHIC-rarity cards (Sox, Landon) share one separate, much rarer roll -
 * that roll picks one of them at random - and the Ender Dragon always drops
 * one of them too (also picked at random), bypassing every other roll
 * entirely. Killing an Enderman, Warden, or Wither also rolls a separate,
 * much bigger bonus chance for one random RARE-or-LEGENDARY card, on top of
 * the tiny chance every hostile mob already has - this is the main way
 * rare/legendary cards are meant to show up, without needing to inflate the
 * base chance for common mobs and flooding the game with clutter. Looting on
 * the killer's weapon boosts every one of these chances (see
 * {@code lootingBonusPerLevel}).
 *
 * This uses Fabric API's ServerLivingEntityEvents.AFTER_DEATH callback rather
 * than a mixin: it already fires once per death on the server (so it is safe
 * on a dedicated server and doesn't run client-side / duplicate drops), and
 * it doesn't require touching vanilla loot tables at all. If you would rather
 * drive drops through data-pack loot tables instead (e.g. to let resource-pack
 * authors/datapack authors tune odds per-mob), replace this class with a
 * LootTableEvents.MODIFY listener that appends a LootPoolEntry to each
 * "entities/*" loot table whose associated EntityType has MobCategory.MONSTER -
 * the per-rarity chance lookup below can be reused as-is.
 */
public final class CardDropHandler {

	private static final Random RANDOM = new Random();

	private CardDropHandler() {
	}

	public static void initialize() {
		ServerLivingEntityEvents.AFTER_DEATH.register(CardDropHandler::onEntityDeath);
	}

	private static void onEntityDeath(LivingEntity entity, DamageSource damageSource) {
		Level level = entity.level();
		if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
			return;
		}

		if (TPIConfig.INSTANCE.requirePlayerKill && !(damageSource.getEntity() instanceof Player)) {
			return;
		}

		// The Ender Dragon always drops one mythic card (picked at random), regardless of every other roll.
		if (entity instanceof EnderDragon) {
			dropCard(serverLevel, entity, pickRandomCard(EnumSet.of(CardRarity.MYTHIC)));
			return;
		}

		// Only regular hostile mobs (creepers, zombies, skeletons, etc.) drop cards.
		if (entity.getType().getCategory() != MobCategory.MONSTER) {
			return;
		}

		double chanceMultiplier = 1.0 + lootingLevel(serverLevel, damageSource) * TPIConfig.INSTANCE.lootingBonusPerLevel;

		// Separate, much rarer roll shared by every mythic card - independent of the per-card rolls below.
		if (RANDOM.nextDouble() < scaledChance(TPIConfig.INSTANCE.mythicCardDropChance, chanceMultiplier)) {
			dropCard(serverLevel, entity, pickRandomCard(EnumSet.of(CardRarity.MYTHIC)));
		}

		// Every normal card gets its own independent roll, chance based on rarity.
		for (Item card : ModItems.ALL_CARDS) {
			CardRarity rarity = ((TradingCardItem) card).getRarity();
			if (rarity == CardRarity.MYTHIC) {
				continue; // handled separately above
			}

			if (RANDOM.nextDouble() < scaledChance(chanceForRarity(rarity), chanceMultiplier)) {
				dropCard(serverLevel, entity, card);
			}
		}

		// Big bonus chance for a rare-or-legendary card from these three notable mobs specifically.
		double bonusChance = specialMobBonusChance(entity);
		if (bonusChance > 0.0 && RANDOM.nextDouble() < scaledChance(bonusChance, chanceMultiplier)) {
			dropCard(serverLevel, entity, pickWeightedRareOrLegendary());
		}
	}

	/**
	 * Weight per rarity within the Enderman/Warden/Wither bonus pool - RARE cards are
	 * 3x as likely as LEGENDARY ones per card, roughly matching the ~2.67:1 ratio between
	 * rareCardDropChance and legendaryCardDropChance above. A plain uniform pick across
	 * the combined pool would actually make LEGENDARY *more* likely than RARE overall,
	 * since there are more legendary cards (5) than rare ones (4) - this keeps legendary
	 * meaningfully rarer than rare, both per-card and in aggregate, like the tier names imply.
	 */
	private static final Map<CardRarity, Integer> BONUS_POOL_WEIGHTS = Map.of(CardRarity.RARE, 3, CardRarity.LEGENDARY, 1);

	private static Item pickWeightedRareOrLegendary() {
		List<Item> candidates = ModItems.ALL_CARDS.stream()
				.filter(card -> BONUS_POOL_WEIGHTS.containsKey(((TradingCardItem) card).getRarity()))
				.toList();

		int totalWeight = candidates.stream().mapToInt(card -> BONUS_POOL_WEIGHTS.get(((TradingCardItem) card).getRarity())).sum();
		int roll = RANDOM.nextInt(totalWeight);
		int cumulativeWeight = 0;
		for (Item card : candidates) {
			cumulativeWeight += BONUS_POOL_WEIGHTS.get(((TradingCardItem) card).getRarity());
			if (roll < cumulativeWeight) {
				return card;
			}
		}
		return candidates.get(candidates.size() - 1); // unreachable, keeps the compiler happy
	}

	private static double specialMobBonusChance(LivingEntity entity) {
		if (entity instanceof WitherBoss) {
			return TPIConfig.INSTANCE.witherRareOrLegendaryBonusChance;
		} else if (entity instanceof Warden) {
			return TPIConfig.INSTANCE.wardenRareOrLegendaryBonusChance;
		} else if (entity instanceof EnderMan) {
			return TPIConfig.INSTANCE.endermanRareOrLegendaryBonusChance;
		}

		return 0.0;
	}

	/** Picks one card at random from among the given rarities - a shared roll's odds just split further as more cards join a given rarity. */
	private static Item pickRandomCard(Set<CardRarity> rarities) {
		List<Item> candidates = ModItems.ALL_CARDS.stream()
				.filter(card -> rarities.contains(((TradingCardItem) card).getRarity()))
				.toList();
		return candidates.get(RANDOM.nextInt(candidates.size()));
	}

	/** Looting level of the killing blow's attacker, or 0 if it wasn't a living entity (or had no Looting). */
	private static int lootingLevel(ServerLevel serverLevel, DamageSource damageSource) {
		if (!(damageSource.getEntity() instanceof LivingEntity attacker)) {
			return 0;
		}

		Holder<Enchantment> looting = serverLevel.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING);
		return EnchantmentHelper.getEnchantmentLevel(looting, attacker);
	}

	private static double scaledChance(double baseChance, double multiplier) {
		return Math.min(1.0, baseChance * multiplier);
	}

	private static double chanceForRarity(CardRarity rarity) {
		return switch (rarity) {
			case COMMON -> TPIConfig.INSTANCE.commonCardDropChance;
			case UNCOMMON -> TPIConfig.INSTANCE.uncommonCardDropChance;
			case RARE -> TPIConfig.INSTANCE.rareCardDropChance;
			case LEGENDARY -> TPIConfig.INSTANCE.legendaryCardDropChance;
			case MYTHIC -> 0.0; // never rolled here - see mythicCardDropChance above
		};
	}

	private static void dropCard(ServerLevel serverLevel, LivingEntity entity, Item card) {
		ItemStack stack = new ItemStack(card, 1);
		ItemEntity itemEntity = new ItemEntity(serverLevel, entity.getX(), entity.getY(), entity.getZ(), stack);
		serverLevel.addFreshEntity(itemEntity);
	}
}
