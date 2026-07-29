package net.tpi.tradingcards.voicechat;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import de.maxhenkel.voicechat.api.ServerPlayer;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.PlayerDisconnectedEvent;
import de.maxhenkel.voicechat.api.packets.MicrophonePacket;

import net.tpi.tradingcards.TPITradingCards;
import net.tpi.tradingcards.item.MicrophoneItem;

/**
 * Simple Voice Chat addon: discovered and loaded by SVC itself (never by us)
 * through the "voicechat" Fabric entrypoint declared in fabric.mod.json, so
 * this class is only ever touched when SVC is actually installed - a server
 * without it never references anything in this package.
 *
 * Verified against the real voicechat-api 2.6.20 classes (decompiled from
 * the installed voicechat-fabric jar, not guessed): MicrophonePacketEvent
 * fires exactly once per incoming mic frame from the speaker, before it's
 * broadcast to anyone in range (see PluginManager#onMicPacket), so mutating
 * the packet's Opus data here changes what every listener hears in one place.
 */
public class TpiVoicechatPlugin implements VoicechatPlugin {

	private VoicechatApi api;
	private final Map<UUID, PlayerAutotuneState> autotuneStates = new ConcurrentHashMap<>();

	@Override
	public String getPluginId() {
		return TPITradingCards.MOD_ID;
	}

	@Override
	public void initialize(VoicechatApi api) {
		this.api = api;
	}

	@Override
	public void registerEvents(EventRegistration registration) {
		registration.registerEvent(MicrophonePacketEvent.class, this::onMicrophonePacket);
		registration.registerEvent(PlayerDisconnectedEvent.class, this::onPlayerDisconnected);
	}

	private void onMicrophonePacket(MicrophonePacketEvent event) {
		VoicechatConnection sender = event.getSenderConnection();
		if (sender == null) {
			return;
		}

		ServerPlayer voicechatPlayer = sender.getPlayer();
		if (!(voicechatPlayer.getPlayer() instanceof net.minecraft.server.level.ServerPlayer player)) {
			return;
		}

		if (!isHoldingMicrophone(player)) {
			return;
		}

		MicrophonePacket packet = event.getPacket();
		PlayerAutotuneState state = autotuneStates.computeIfAbsent(voicechatPlayer.getUuid(), id -> new PlayerAutotuneState(api));
		short[] pcm = state.decoder.decode(packet.getOpusEncodedData());
		AutotuneFilter.apply(pcm, state);
		packet.setOpusEncodedData(state.encoder.encode(pcm));
	}

	/** Frees the player's Opus codec state once they leave - otherwise it'd leak for the life of the server. */
	private void onPlayerDisconnected(PlayerDisconnectedEvent event) {
		PlayerAutotuneState state = autotuneStates.remove(event.getPlayerUuid());
		if (state != null) {
			state.close();
		}
	}

	private static boolean isHoldingMicrophone(net.minecraft.server.level.ServerPlayer player) {
		return player.getMainHandItem().getItem() instanceof MicrophoneItem
				|| player.getOffhandItem().getItem() instanceof MicrophoneItem;
	}
}
