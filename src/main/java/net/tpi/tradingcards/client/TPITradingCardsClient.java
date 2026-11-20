package net.tpi.tradingcards.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.tpi.tradingcards.TPITradingCards;
import net.tpi.tradingcards.entity.BandMemberEntity;
import net.tpi.tradingcards.entity.ModEntities;

/** Client-only setup: entity renderers for the band members and Captain Sox. */
public class TPITradingCardsClient implements ClientModInitializer {

	@Override
	public void onInitializeClient() {
		for (EntityType<BandMemberEntity> type : ModEntities.BAND_MEMBERS) {
			String member = BuiltInRegistries.ENTITY_TYPE.getKey(type).getPath();
			Identifier skin = Identifier.fromNamespaceAndPath(TPITradingCards.MOD_ID, "entity/band/" + member);
			EntityRendererRegistry.register(type, context -> new BandMemberRenderer(context, skin));
		}
		EntityRendererRegistry.register(ModEntities.CAPTAIN_SOX, CaptainSoxRenderer::new);
	}
}
