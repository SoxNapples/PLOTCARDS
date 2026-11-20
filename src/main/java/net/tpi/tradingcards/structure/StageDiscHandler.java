package net.tpi.tradingcards.structure;

import java.util.List;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.tpi.tradingcards.item.ModDiscs;

/**
 * Gives every concert stage a random disc in its item frame.
 *
 * The stage structure file (data/tpi_trading_cards/structure/concert_stage.nbt) stores
 * its framed disc with custom_data {tpi_random_disc: 1b}. When that frame is first loaded
 * into the world - i.e. right after the stage generates - this swaps the placeholder for
 * a random disc from ModDiscs.STAGE_LOOT_DISCS (the uncraftable part of the discography).
 * The swapped disc has no marker, so it's only ever rolled once per stage.
 *
 * If this somehow didn't run, the frame still holds a real (Wife Beater) disc.
 */
public final class StageDiscHandler {

	public static final String MARKER = "tpi_random_disc";

	private StageDiscHandler() {
	}

	public static void initialize() {
		ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> {
			if (!(entity instanceof ItemFrame frame)) {
				return;
			}
			ItemStack framed = frame.getItem();
			CustomData data = framed.get(DataComponents.CUSTOM_DATA);
			if (data == null || !data.copyTag().contains(MARKER)) {
				return;
			}
			List<Item> pool = ModDiscs.STAGE_LOOT_DISCS;
			if (pool.isEmpty()) {
				framed.remove(DataComponents.CUSTOM_DATA);
				return;
			}
			Item disc = pool.get(level.getRandom().nextInt(pool.size()));
			frame.setItem(new ItemStack(disc), false);
		});
	}
}
