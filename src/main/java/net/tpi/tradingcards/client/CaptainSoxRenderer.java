package net.tpi.tradingcards.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.animal.feline.AbstractFelineModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
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
 * (black-and-white) cat texture, plus a captain's pirate hat (bicorne) on his
 * head with his ears poking out in front of it.
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

	/** The captain's hat; follows the cat's head (including while sitting / lying on your bed). */
	static class PirateHatLayer extends RenderLayer<CatRenderState, AbstractFelineModel<CatRenderState>> {

		private static final Identifier HAT_TEXTURE =
				Identifier.fromNamespaceAndPath(TPITradingCards.MOD_ID, "textures/entity/captain_sox_hat.png");
		private static final RenderType HAT_RENDER_TYPE = RenderTypes.entityCutout(HAT_TEXTURE);

		private final ModelPart hat = createHat();

		PirateHatLayer(CatRenderer renderer) {
			super(renderer);
		}

		/**
		 * Captain's hat, in the cat head's local space (head cube spans x -2.5..2.5, y -2..2, z -3..2;
		 * the face is at -z, the cat's right side is -x):
		 *   - big half-moon bicorne panel, much wider than the head - a thin box whose front/back
		 *     faces carry the half-moon shape as a cutout, with gold trim and a skull and crossbones
		 *   - red band around the base (also covers the tiny vanilla ears)
		 *   - bigger black ears that poke up IN FRONT of the hat
		 *   - red ribbon knot + tail dangling on his right side
		 * The texture is painted at 2x (128x32 image for a 64x16 UV space) so the skull stays crisp.
		 */
		private static ModelPart createHat() {
			MeshDefinition mesh = new MeshDefinition();
			mesh.getRoot().addOrReplaceChild("hat", CubeListBuilder.create()
							.texOffs(0, 0).addBox(-6.0F, -9.0F, 0.5F, 12.0F, 6.0F, 1.0F)    // half-moon panel
							.texOffs(0, 8).addBox(-3.0F, -3.0F, -0.5F, 6.0F, 1.0F, 3.0F)    // red band
							.texOffs(20, 8).addBox(-2.6F, -5.0F, -1.5F, 2.0F, 3.0F, 1.0F)   // right ear
							.texOffs(28, 8).addBox(0.6F, -5.0F, -1.5F, 2.0F, 3.0F, 1.0F)    // left ear
							.texOffs(36, 8).addBox(-3.6F, -3.2F, -0.6F, 1.0F, 2.0F, 2.0F)   // ribbon knot
							.texOffs(44, 8).addBox(-3.9F, -1.4F, -0.3F, 1.0F, 3.0F, 1.0F),  // ribbon tail
					PartPose.ZERO);
			return LayerDefinition.create(mesh, 64, 16).bakeRoot();
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
