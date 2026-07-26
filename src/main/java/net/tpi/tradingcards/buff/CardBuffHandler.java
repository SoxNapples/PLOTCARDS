package net.tpi.tradingcards.buff;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;

/** Periodically refreshes CardBuffs.tick for every online player. Runs server-side only (harmless no-op on client). */
public final class CardBuffHandler {

	private CardBuffHandler() {
	}

	/** Refresh interval in ticks - comfortably shorter than CardBuffs' own effect duration so buffs never visibly lapse. */
	private static final int CHECK_INTERVAL_TICKS = 10;

	private static int tickCounter = 0;

	public static void initialize() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			tickCounter++;
			if (tickCounter % CHECK_INTERVAL_TICKS != 0) {
				return;
			}
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				CardBuffs.tick(player);
			}
		});
	}
}
