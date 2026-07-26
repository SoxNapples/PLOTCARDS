package net.tpi.tradingcards.mixin.client;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import net.tpi.tradingcards.item.TradingCardItem;
import net.tpi.tradingcards.mixin.TpiCardFrameStateAccess;

/**
 * Makes trading cards render big in item frames, the same way a filled map
 * does, instead of the standard small centered item icon - reusing vanilla's
 * own frame-positioning logic (shadowed/copied below) verbatim from the
 * decompiled 26.2 ItemFrameRenderer#submit, just swapping the final "draw the
 * map" step for our own textured quad sized to the card's aspect ratio.
 *
 * ItemFrameRenderState only carries pre-extracted render data, not the live
 * ItemStack, so extractRenderState (which does have it) stashes what's needed
 * onto the state via TpiCardFrameStateAccess for submit() to read back later.
 */
@Mixin(ItemFrameRenderer.class)
public abstract class ItemFrameRendererMixin {

	@Shadow
	public abstract Vec3 getRenderOffset(ItemFrameRenderState state);

	@Shadow
	private int getLightCoords(boolean isGlowFrame, int glowLightCoords, int originalLightCoords) {
		throw new UnsupportedOperationException();
	}

	@Unique
	private static final Map<Identifier, RenderType> TPI_FRAME_CARD_RENDER_TYPES = new HashMap<>();

	/**
	 * Vanilla only floats a name label over a framed item when it has an explicit custom name (renamed via anvil) -
	 * see ItemFrameRenderer#shouldShowName in the decompiled source. Cards never get renamed, so without this
	 * override the hologram never shows even though every card already resolves a proper display name (via lang
	 * file entries) through getNameTag - that part needs no change.
	 */
	@Inject(method = "shouldShowName", at = @At("HEAD"), cancellable = true)
	private void tpi$showCardNameOnHover(ItemFrame entity, double distanceToCameraSq, CallbackInfoReturnable<Boolean> cir) {
		if (entity.getItem().getItem() instanceof TradingCardItem) {
			Minecraft minecraft = Minecraft.getInstance();
			boolean visible = !minecraft.gui.hud.isHidden() && minecraft.getEntityRenderDispatcher().crosshairPickEntity == entity;
			cir.setReturnValue(visible);
		}
	}

	@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void tpi$extractCardState(ItemFrame entity, ItemFrameRenderState state, float partialTicks, CallbackInfo ci) {
		TpiCardFrameStateAccess cardState = (TpiCardFrameStateAccess) state;
		ItemStack itemStack = entity.getItem();
		if (itemStack.getItem() instanceof TradingCardItem card) {
			Identifier itemId = BuiltInRegistries.ITEM.getKey(card);
			cardState.tpi$setIsTradingCard(true);
			cardState.tpi$setCardTextureId(Identifier.fromNamespaceAndPath(itemId.getNamespace(), "textures/item/" + itemId.getPath() + ".png"));
			cardState.tpi$setCardAspect(card.getAspectRatio());
		} else {
			cardState.tpi$setIsTradingCard(false);
		}
	}

	@Inject(method = "submit", at = @At("HEAD"), cancellable = true)
	private void tpi$interceptCardFrame(
			ItemFrameRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
		TpiCardFrameStateAccess cardState = (TpiCardFrameStateAccess) state;
		if (state.mapId != null || !cardState.tpi$isTradingCard()) {
			return;
		}

		// Vanilla's own submit() starts with super.submit(...), which is what actually renders the floating name
		// hologram (via EntityRenderer#submitNameDisplay) - cancelling HEAD skips that call entirely, so it has to
		// be replicated here verbatim (same order, same fields) or the card's name never shows when hovered.
		poseStack.pushPose();
		if (state.scoreText != null) {
			submitNodeCollector.submitNameTag(poseStack, state.nameTagAttachment, 0, state.scoreText, !state.isDiscrete, state.lightCoords, camera);
			poseStack.translate(0.0F, 9.0F * 1.15F * 0.025F, 0.0F);
		}
		if (state.nameTag != null) {
			submitNodeCollector.submitNameTag(poseStack, state.nameTagAttachment, 0, state.nameTag, !state.isDiscrete, state.lightCoords, camera);
		}
		poseStack.popPose();

		poseStack.pushPose();
		Direction direction = state.direction;
		Vec3 renderOffset = this.getRenderOffset(state);
		poseStack.translate(-renderOffset.x(), -renderOffset.y(), -renderOffset.z());
		poseStack.translate(direction.getStepX() * 0.46875, direction.getStepY() * 0.46875, direction.getStepZ() * 0.46875);
		float xRot;
		float yRot;
		if (direction.getAxis().isHorizontal()) {
			xRot = 0.0F;
			yRot = 180.0F - direction.toYRot();
		} else {
			xRot = -90 * direction.getAxisDirection().getStep();
			yRot = 180.0F;
		}

		poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
		poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
		if (!state.frameModel.isEmpty()) {
			poseStack.pushPose();
			poseStack.translate(-0.5F, -0.5F, -0.5F);
			state.frameModel.submitWithZOffset(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
			poseStack.popPose();
		}

		if (state.isInvisible) {
			poseStack.translate(0.0F, 0.0F, 0.5F);
		} else {
			poseStack.translate(0.0F, 0.0F, 0.4375F);
		}

		poseStack.mulPose(Axis.ZP.rotationDegrees(state.rotation * 360.0F / 8.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
		poseStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
		poseStack.translate(0.0F, 0.0F, -1.0F);

		int lightVal = this.getLightCoords(state.isGlowFrame, 15728880, state.lightCoords);
		Identifier textureId = cardState.tpi$cardTextureId();
		RenderType renderType = TPI_FRAME_CARD_RENDER_TYPES.computeIfAbsent(textureId, RenderTypes::text);

		float halfHeight = 64.0F;
		float halfWidth = 64.0F * cardState.tpi$cardAspect();

		submitNodeCollector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
			buffer.addVertex(pose, -halfWidth, halfHeight, 0.0F).setColor(-1).setUv(0.0F, 1.0F).setLight(lightVal);
			buffer.addVertex(pose, halfWidth, halfHeight, 0.0F).setColor(-1).setUv(1.0F, 1.0F).setLight(lightVal);
			buffer.addVertex(pose, halfWidth, -halfHeight, 0.0F).setColor(-1).setUv(1.0F, 0.0F).setLight(lightVal);
			buffer.addVertex(pose, -halfWidth, -halfHeight, 0.0F).setColor(-1).setUv(0.0F, 0.0F).setLight(lightVal);
		});

		poseStack.popPose();
		ci.cancel();
	}
}
