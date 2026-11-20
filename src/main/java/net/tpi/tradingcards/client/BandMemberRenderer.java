package net.tpi.tradingcards.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.tpi.tradingcards.entity.BandMemberEntity;

/**
 * Renders a band member with the real (wide-arm) player model - including the
 * outer skin layer (hat, jacket, sleeves, pants) - and a standard 64x64 player
 * skin at textures/entity/band/<member>.png. Scaled like a real player.
 */
public class BandMemberRenderer extends HumanoidMobRenderer<BandMemberEntity, AvatarRenderState, PlayerModel> {

	private final Identifier texture;

	public BandMemberRenderer(EntityRendererProvider.Context context, Identifier texture) {
		super(context, new PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
		this.texture = texture;
	}

	@Override
	public AvatarRenderState createRenderState() {
		return new AvatarRenderState();
	}

	@Override
	public Identifier getTextureLocation(AvatarRenderState state) {
		return texture;
	}

	/** Same 0.9375 scale vanilla applies to real players, so they're exactly player-sized. */
	@Override
	protected void scale(AvatarRenderState state, PoseStack poseStack) {
		poseStack.scale(0.9375F, 0.9375F, 0.9375F);
	}
}
