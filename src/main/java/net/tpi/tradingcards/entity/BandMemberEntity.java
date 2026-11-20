package net.tpi.tradingcards.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

/**
 * A friendly band member NPC - one entity type per member (see ModEntities), all
 * sharing this class. Rendered with the vanilla player model and a custom skin.
 *
 * Behaves like a wandering villager: strolls around, looks at nearby players,
 * panics when hit and keeps away from hostile mobs. Never attacks anything.
 *
 * Spawns naturally only during the day, on grass-type blocks under open sky
 * (see checkBandMemberSpawnRules). Like a wandering trader it despawns once no
 * player is nearby, so random spawns don't pile up over time - unless it's been
 * named with a name tag, which makes it stay.
 */
public class BandMemberEntity extends PathfinderMob {

	public BandMemberEntity(EntityType<? extends BandMemberEntity> type, Level level) {
		super(type, level);
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Mob.createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0)
				.add(Attributes.MOVEMENT_SPEED, 0.3);
	}

	@Override
	protected void registerGoals() {
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(1, new PanicGoal(this, 1.5));
		this.goalSelector.addGoal(2, new AvoidEntityGoal<>(this, Monster.class, 8.0F, 0.9, 1.3));
		this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
		this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8.0F));
		this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
	}

	/** Natural spawns: grass-type ground, bright, open sky, and only while it's daytime. Spawn eggs / commands work any time. */
	public static boolean checkBandMemberSpawnRules(
			EntityType<BandMemberEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
		if (!level.getBlockState(pos.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON)) {
			return false;
		}
		if (reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION) {
			return level.getLevel().isBrightOutside() && level.canSeeSky(pos) && level.getRawBrightness(pos, 0) > 8;
		}
		return true;
	}

	/** Despawn when players are far away, like a wandering trader (name-tagged ones are kept by vanilla's persistence check). */
	@Override
	public boolean removeWhenFarAway(double distSqr) {
		return true;
	}

	/** Always show the member's name above their head, like a real player's nametag. */
	@Override
	public boolean shouldShowName() {
		return true;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource source) {
		return SoundEvents.PLAYER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.PLAYER_DEATH;
	}
}
