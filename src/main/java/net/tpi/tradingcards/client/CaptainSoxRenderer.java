package net.tpi.tradingcards.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.animal.feline.AbstractFelineModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.CatRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.CatRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.animal.feline.Cat;
import net.tpi.tradingcards.TPITradingCards;

/**
 * Captain Sox: the vanilla cat renderer, locked to vanilla's own tuxedo
 * (black-and-white) cat texture, plus a pirate tricorn layer on his head.
 * The hat is our own model + texture (textures/entity/captain_sox_hat.png).
 */
public class CaptainSoxRenderer extends CatRenderer {

	/** Vanilla's tuxedo cat ("black" cat variant) - referenced from the game, not copied into the mod. */
	private static final Identifier TUXEDO = Identifier.withDefaultNamespace("textures/entity/cat/cat_black.png");
	private static final Identifier TUXEDO_BABY = Identifier.withDefaultNamespace("textures/entity/cat/cat_black_baby.png");

	public CaptainSoxRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.addLayer(new PirateHatLayer(this));
	}

	@Override
	public void extractRenderState(Cat entity, CatRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.texture = state.isBaby ? TUXEDO_BABY : TUXEDO;
	}

	/** A small pirate tricorn that follows the cat's head (including while sitting / lying on your bed). */
	static class PirateHatLayer extends RenderLayer<CatRenderState, AbstractFelineModel<CatRenderState>> {

		private static final Identifier HAT_TEXTURE =
				Identifier.fromNamespaceAndPath(TPITradingCards.MOD_ID, "textures/entity/captain_sox_hat.png");
		private static final RenderType HAT_RENDER_TYPE = RenderTypes.entityCutout(HAT_TEXTURE);

		private final ModelPart hat = createHat();

		PirateHatLayer(CatRenderer renderer) {
			super(renderer);
		}

		/** Hat geometry, in the cat head's local space (head cube spans x -2.5..2.5, y -2..2, z -3..2). */
		private static ModelPart createHat() {
			MeshDefinition mesh = new MeshDefinition();
			mesh.getRoot().addOrReplaceChild("hat", CubeListBuilder.create()
							.texOffs(0, 0).addBox(-3.5F, -3.0F, -4.0F, 7.0F, 1.0F, 7.0F, new CubeDeformation(0.1F))   // brim, inflated to cleanly cover the ears
							.texOffs(0, 8).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 2.0F, 4.0F),  // crown
					PartPose.ZERO);
			return LayerDefinition.create(mesh, 32, 16).bakeRoot();
		}

		@Override
		public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords,
				CatRenderState state, float yRot, float xRot) {
			if (state.isInvisible) {
				return;
			}
			ModelPart root = this.getParentModel().root();
			poseStack.pushPose();
			root.translateAndRotate(poseStack);
			root.getChild("head").translateAndRotate(poseStack);
			submitNodeCollector.submitModelPart(this.hat, poseStack, HAT_RENDER_TYPE, lightCoords, OverlayTexture.NO_OVERLAY, null);
			poseStack.popPose();
		}
	}
}
