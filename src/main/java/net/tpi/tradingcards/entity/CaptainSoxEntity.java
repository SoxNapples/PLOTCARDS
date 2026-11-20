package net.tpi.tradingcards.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.tpi.tradingcards.config.TPIConfig;
import net.tpi.tradingcards.item.ModItems;

/**
 * Captain Sox - a black-and-white (tuxedo) cat in a pirate hat, the mascot on
 * the Sox trading card.
 *
 * Extends the vanilla cat, so he behaves exactly like one: tame him and he sits,
 * follows, sleeps on your bed, scares creepers and phantoms, and so on. The
 * differences:
 *   - always rendered as a tuxedo cat with a pirate tricorn (see CaptainSoxRenderer)
 *   - tamed/healed with raw or cooked cod, salmon or chicken (vanilla cats only take raw fish)
 *   - his name shows above his head
 *   - he's one of a kind: he doesn't breed
 *   - small chance (config: captainSoxCardDropChance) to drop the Sox trading card when he dies
 *
 * Untamed, he wanders off (despawns) like any untamed cat when no player is near.
 */
public class CaptainSoxEntity extends Cat {

	public CaptainSoxEntity(EntityType<? extends Cat> type, Level level) {
		super(type, level);
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return stack.is(ItemTags.CAT_FOOD)
				|| stack.is(Items.COD) || stack.is(Items.COOKED_COD)
				|| stack.is(Items.SALMON) || stack.is(Items.COOKED_SALMON)
				|| stack.is(Items.CHICKEN) || stack.is(Items.COOKED_CHICKEN);
	}

	@Override
	public boolean canMate(Animal partner) {
		return false;
	}

	@Override
	public Cat getBreedOffspring(ServerLevel level, AgeableMob partner) {
		return null;
	}

	@Override
	public boolean shouldShowName() {
		return true;
	}

	@Override
	protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
		super.dropCustomDeathLoot(level, source, killedByPlayer);
		if (this.random.nextDouble() < TPIConfig.INSTANCE.captainSoxCardDropChance) {
			this.spawnAtLocation(level, ModItems.SOX_CARD);
		}
	}
}
