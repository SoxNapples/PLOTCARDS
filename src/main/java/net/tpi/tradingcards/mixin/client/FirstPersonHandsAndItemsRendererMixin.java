package net.tpi.tradingcards.mixin.client;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

import net.tpi.tradingcards.item.TradingCardItem;

/**
 * Makes trading cards render held up with both hands, facing the camera, the
 * same way vanilla renders a filled map - instead of the standard "flat icon
 * stuck to the arm" first-person item render.
 *
 * Minecraft 26.3 replaced ItemInHandRenderer with FirstPersonHandsAndItemsRenderer,
 * which renders from extracted render state (PlayerRenderState /
 * FirstPersonHandsAndItemsRenderState) instead of the live player. All the
 * pose-stack math below is copied from its renderTwoHandedMap/renderOneHandedMap
 * in the decompiled 26.3 client jar; the only thing swapped out is the final
 * textured quad, which binds the card's own item texture instead of map data.
 */
@Mixin(FirstPersonHandsAndItemsRenderer.class)
public abstract class FirstPersonHandsAndItemsRendererMixin {

	@Shadow
	private float calculateMapTilt(float xRot) {
		throw new UnsupportedOperationException();
	}

	@Shadow
	private void renderMapHand(
			PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, HumanoidArm arm, PlayerRenderState playerState) {
		throw new UnsupportedOperationException();
	}

	@Shadow
	private void renderPlayerArm(
			PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, float inverseArmHeight, float attackValue,
			HumanoidArm arm, PlayerRenderState playerState) {
		throw new UnsupportedOperationException();
	}

	@Unique
	private static final Map<Identifier, RenderType> TPI_CARD_RENDER_TYPES = new HashMap<>();

	@Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
	private void tpi$interceptCardHold(
			PlayerRenderState playerState, FirstPersonHandsAndItemsRenderState state, float partialTicks, float xRot,
			InteractionHand hand, float attack, ItemStack itemStack, float inverseArmHeight, PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector, int lightCoords, CallbackInfo ci) {
		if (state.isScoping || !(itemStack.getItem() instanceof TradingCardItem)) {
			return;
		}

		AvatarRenderState avatar = playerState.avatarRenderState;
		if (avatar == null) {
			return;
		}

		boolean isMainHand = hand == InteractionHand.MAIN_HAND;
		HumanoidArm arm = isMainHand ? avatar.mainArm : avatar.mainArm.getOpposite();

		poseStack.pushPose();
		if (isMainHand && state.offHandItem.isEmpty()) {
			this.tpi$renderTwoHandedCard(poseStack, submitNodeCollector, lightCoords, xRot, inverseArmHeight, attack, itemStack, playerState, avatar);
		} else {
			this.tpi$renderOneHandedCard(poseStack, submitNodeCollector, lightCoords, inverseArmHeight, arm, attack, itemStack, playerState, avatar);
		}

		poseStack.popPose();
		ci.cancel();
	}

	@Unique
	private void tpi$renderTwoHandedCard(
			PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
			float xRot, float inverseArmHeight, float attackValue, ItemStack itemStack,
			PlayerRenderState playerState, AvatarRenderState avatar) {
		float sqrtAttackValue = Mth.sqrt(attackValue);
		float ySwingPosition = -0.2F * Mth.sin(attackValue * (float) Math.PI);
		float zSwingPosition = -0.4F * Mth.sin(sqrtAttackValue * (float) Math.PI);
		poseStack.translate(0.0F, -ySwingPosition / 2.0F, zSwingPosition);
		float mapTilt = this.calculateMapTilt(xRot);
		poseStack.translate(0.0F, 0.04F + inverseArmHeight * -1.2F + mapTilt * -0.5F, -0.72F);
		poseStack.rotateDegrees(Axis.XP, mapTilt * -85.0F);
		if (!avatar.isInvisible) {
			poseStack.pushPose();
			poseStack.rotateDegrees(Axis.YP, 90.0F);
			this.renderMapHand(poseStack, submitNodeCollector, lightCoords, HumanoidArm.RIGHT, playerState);
			this.renderMapHand(poseStack, submitNodeCollector, lightCoords, HumanoidArm.LEFT, playerState);
			poseStack.popPose();
		}

		float xzSwingRotation = Mth.sin(sqrtAttackValue * (float) Math.PI);
		poseStack.rotateDegrees(Axis.XP, xzSwingRotation * 20.0F);
		poseStack.scale(2.0F, 2.0F, 2.0F);
		this.tpi$renderCard(poseStack, submitNodeCollector, lightCoords, itemStack);
	}

	@Unique
	private void tpi$renderOneHandedCard(
			PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
			float inverseArmHeight, HumanoidArm arm, float attackValue, ItemStack itemStack,
			PlayerRenderState playerState, AvatarRenderState avatar) {
		float invert = arm == HumanoidArm.RIGHT ? 1.0F : -1.0F;
		poseStack.translate(invert * 0.125F, -0.125F, 0.0F);
		if (!avatar.isInvisible) {
			poseStack.pushPose();
			poseStack.rotateDegrees(Axis.ZP, invert * 10.0F);
			this.renderPlayerArm(poseStack, submitNodeCollector, lightCoords, inverseArmHeight, attackValue, arm, playerState);
			poseStack.popPose();
		}

		poseStack.pushPose();
		poseStack.translate(invert * 0.51F, -0.08F + inverseArmHeight * -1.2F, -0.75F);
		float sqrtAttackValue = Mth.sqrt(attackValue);
		float xSwing = Mth.sin(sqrtAttackValue * (float) Math.PI);
		float xSwingPosition = -0.5F * xSwing;
		float ySwingPosition = 0.4F * Mth.sin(sqrtAttackValue * (float) (Math.PI * 2));
		float zSwingPosition = -0.3F * Mth.sin(attackValue * (float) Math.PI);
		poseStack.translate(invert * xSwingPosition, ySwingPosition - 0.3F * xSwing, zSwingPosition);
		poseStack.rotateDegrees(Axis.XP, xSwing * -45.0F);
		poseStack.rotateDegrees(Axis.YP, invert * xSwing * -30.0F);
		this.tpi$renderCard(poseStack, submitNodeCollector, lightCoords, itemStack);
		poseStack.popPose();
	}

	/** Same quad geometry vanilla uses for a held map, just narrower to match the card's aspect ratio, and bound to the card's own texture instead of live map data. */
	@Unique
	private void tpi$renderCard(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, ItemStack itemStack) {
		poseStack.rotateDegrees(Axis.YP, 180.0F);
		poseStack.rotateDegrees(Axis.ZP, 180.0F);
		poseStack.scale(0.38F, 0.38F, 0.38F);
		poseStack.translate(-0.5F, -0.5F, 0.0F);
		poseStack.scale(0.0078125F, 0.0078125F, 0.0078125F);

		Identifier itemId = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
		Identifier textureId = Identifier.fromNamespaceAndPath(itemId.getNamespace(), "textures/item/" + itemId.getPath() + ".png");
		RenderType renderType = TPI_CARD_RENDER_TYPES.computeIfAbsent(textureId, RenderTypes::text);

		TradingCardItem card = (TradingCardItem) itemStack.getItem();
		float aspect = card.getAspectRatio();
		float halfWidth = 142.0F * aspect / 2.0F;
		float xMin = 64.0F - halfWidth;
		float xMax = 64.0F + halfWidth;

		submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
			buffer.addVertex(pose, xMin, 135.0F, 0.0F).setColor(-1).setUv(0.0F, 1.0F).setLight(lightCoords);
			buffer.addVertex(pose, xMax, 135.0F, 0.0F).setColor(-1).setUv(1.0F, 1.0F).setLight(lightCoords);
			buffer.addVertex(pose, xMax, -7.0F, 0.0F).setColor(-1).setUv(1.0F, 0.0F).setLight(lightCoords);
			buffer.addVertex(pose, xMin, -7.0F, 0.0F).setColor(-1).setUv(0.0F, 0.0F).setLight(lightCoords);
		});
	}
}
