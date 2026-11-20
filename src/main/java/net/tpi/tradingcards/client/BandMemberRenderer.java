package net.tpi.tradingcards.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.tpi.tradingcards.entity.BandMemberEntity;

/**
 * Renders a band member with the real (wide-arm) player model - including the
 * outer skin layer (hat, jacket, sleeves, pants) - and a standard 64x64 player
 * skin at textures/entity/band/<member>.png. Scaled like a real player.
 *
 * The skin is set in two places: getTextureLocation (what vanilla's renderer
 * uses) AND AvatarRenderState.skin. Player-skin code - and mods like Entity
 * Texture Features / Entity Model Features - read the skin from the render
 * state, which defaults to Steve, so leaving it unset showed a Steve skin.
 */
public class BandMemberRenderer extends HumanoidMobRenderer<BandMemberEntity, AvatarRenderState, PlayerModel> {

	private final Identifier texture;
	private final PlayerSkin skin;

	/** @param assetId skin id without "textures/" and ".png", e.g. tpi_trading_cards:entity/band/landon_tewers */
	public BandMemberRenderer(EntityRendererProvider.Context context, Identifier assetId) {
		super(context, new PlayerModel(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
		this.texture = assetId.withPath(path -> "textures/" + path + ".png");
		this.skin = new PlayerSkin(new ClientAsset.ResourceTexture(assetId, this.texture), null, null, PlayerModelType.WIDE, true);
	}

	@Override
	public void extractRenderState(BandMemberEntity entity, AvatarRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.skin = this.skin;
		state.id = entity.getId();
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
