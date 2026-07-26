package net.tpi.tradingcards.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Right-clicking while not targeting a placeable surface plays the vanilla
 * eating animation (via the CONSUMABLE component attached in ModBlocks), so it
 * looks like the player is singing into the mic. finishUsingItem is overridden
 * to skip Consumable's default stack.consume(1, user) - the mic is a
 * reusable prop, not something that gets eaten up.
 *
 * Hitting an entity with it sends them flying - a big physics-based knockback
 * impulse (not a teleport), tuned to roughly 10 blocks on open flat ground.
 * Real distance varies with terrain/obstacles/knockback resistance, same as
 * any other knockback in Minecraft, just scaled way up.
 */
public class MicrophoneItem extends BlockItem {

	/** Tuned so an unobstructed hit launches the target roughly 10 blocks before drag/gravity bring it back down. */
	private static final double KNOCKBACK_STRENGTH = 4.0;

	public MicrophoneItem(Block block, Properties properties) {
		super(block, properties);
	}

	@Override
	public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
		return stack;
	}

	@Override
	public void hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity attacker) {
		double dx = attacker.getX() - target.getX();
		double dz = attacker.getZ() - target.getZ();
		target.knockback(KNOCKBACK_STRENGTH, dx, dz, attacker.damageSources().generic(), 0.0F, false);
	}
}
