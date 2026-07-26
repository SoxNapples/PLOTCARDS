package net.tpi.tradingcards.mixin;

import net.minecraft.resources.Identifier;

/**
 * Duck-typed access to the extra fields ItemFrameRenderStateMixin adds onto
 * ItemFrameRenderState - stashed during ItemFrameRenderer#extractRenderState
 * (which has the real ItemStack) so ItemFrameRendererMixin#submit (which only
 * gets the already-extracted state) knows whether to render a trading card big,
 * like a map, instead of the normal small item-in-frame icon.
 *
 * Deliberately NOT in net.tpi.tradingcards.mixin.client - that whole package is
 * claimed by tpi_trading_cards.client.mixins.json as a mixin package, and Mixin
 * refuses to let ordinary code load/reference any class inside it directly.
 */
public interface TpiCardFrameStateAccess {

	boolean tpi$isTradingCard();

	void tpi$setIsTradingCard(boolean value);

	Identifier tpi$cardTextureId();

	void tpi$setCardTextureId(Identifier textureId);

	float tpi$cardAspect();

	void tpi$setCardAspect(float aspect);
}
