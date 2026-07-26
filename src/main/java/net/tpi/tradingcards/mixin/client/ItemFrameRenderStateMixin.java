package net.tpi.tradingcards.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.renderer.entity.state.ItemFrameRenderState;
import net.minecraft.resources.Identifier;

import net.tpi.tradingcards.mixin.TpiCardFrameStateAccess;

@Mixin(ItemFrameRenderState.class)
public class ItemFrameRenderStateMixin implements TpiCardFrameStateAccess {

	@Unique
	private boolean tpi$isTradingCard;

	@Unique
	private Identifier tpi$cardTextureId;

	@Unique
	private float tpi$cardAspect;

	@Override
	public boolean tpi$isTradingCard() {
		return tpi$isTradingCard;
	}

	@Override
	public void tpi$setIsTradingCard(boolean value) {
		this.tpi$isTradingCard = value;
	}

	@Override
	public Identifier tpi$cardTextureId() {
		return tpi$cardTextureId;
	}

	@Override
	public void tpi$setCardTextureId(Identifier textureId) {
		this.tpi$cardTextureId = textureId;
	}

	@Override
	public float tpi$cardAspect() {
		return tpi$cardAspect;
	}

	@Override
	public void tpi$setCardAspect(float aspect) {
		this.tpi$cardAspect = aspect;
	}
}
