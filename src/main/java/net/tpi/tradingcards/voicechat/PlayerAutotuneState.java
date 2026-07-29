package net.tpi.tradingcards.voicechat;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.PitchShifter;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.pitch.Yin;
import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.opus.OpusDecoder;
import de.maxhenkel.voicechat.api.opus.OpusEncoder;

/**
 * Per-player Opus codec + TarsosDSP pitch-shifter state, kept alive for as
 * long as the player is connected. Both the Opus codec and the phase-vocoder
 * PitchShifter carry history from one frame to the next (Opus predicts from
 * the previous frame; the shifter tracks phase continuity across frames), so
 * reusing one instance per speaker is what keeps the audio glitch-free -
 * sharing state across simultaneous speakers would corrupt everyone's audio,
 * which is why {@link TpiVoicechatPlugin} keys one of these per player UUID.
 */
final class PlayerAutotuneState {

	/** Simple Voice Chat transmits 48kHz mono PCM, 960 samples (20ms) per frame. */
	static final int FRAME_SIZE = 960;
	static final int SAMPLE_RATE_HZ = 48000;

	/**
	 * Yin needs roughly 2x the period of the lowest frequency it should detect to work reliably - one 960-sample
	 * frame alone only reaches ~100Hz (yinBuffer is half the input), which would miss deeper voices. Feeding it
	 * the last two frames concatenated instead reaches ~50Hz, comfortably covering the full vocal range, at the
	 * cost of the pitch estimate lagging the shifted audio by up to one extra frame.
	 */
	private static final int DETECTION_WINDOW_SIZE = FRAME_SIZE * 2;

	final OpusDecoder decoder;
	final OpusEncoder encoder;
	final PitchShifter pitchShifter;
	final Yin pitchDetector;
	final AudioEvent audioEvent;
	final float[] detectionWindow = new float[DETECTION_WINDOW_SIZE];

	PlayerAutotuneState(VoicechatApi api) {
		this.decoder = api.createDecoder();
		this.encoder = api.createEncoder();
		// overlap=0: each 960-sample frame is its own independent analysis window, processed exactly once as it
		// arrives - trades a bit of smoothing between frames for staying perfectly in sync with SVC's frame rate
		// (no internal buffering/latency drift).
		this.pitchShifter = new PitchShifter(1.0, SAMPLE_RATE_HZ, FRAME_SIZE, 0);
		this.pitchDetector = new Yin(SAMPLE_RATE_HZ, DETECTION_WINDOW_SIZE);
		TarsosDSPAudioFormat format = new TarsosDSPAudioFormat(SAMPLE_RATE_HZ, 16, 1, true, false);
		this.audioEvent = new AudioEvent(format);
	}

	void close() {
		decoder.close();
		encoder.close();
	}
}
