package net.tpi.tradingcards.voicechat;

import java.lang.reflect.Method;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.PitchShifter;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.pitch.Yin;
import de.maxhenkel.voicechat.api.VoicechatApi;

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

	private final Object decoder;
	private final Object encoder;
	private final Method decoderMethod;
	private final Method encoderMethod;
	final PitchShifter pitchShifter;
	final Yin pitchDetector;
	final AudioEvent audioEvent;
	final float[] detectionWindow = new float[DETECTION_WINDOW_SIZE];

	PlayerAutotuneState(VoicechatApi api) {
		this.decoder = createCodec(api, "createDecoder");
		this.encoder = createCodec(api, "createEncoder");
		this.decoderMethod = findMethod(this.decoder, "decode", byte[].class);
		this.encoderMethod = findMethod(this.encoder, "encode", short[].class);
		// overlap=0: each 960-sample frame is its own independent analysis window, processed exactly once as it
		// arrives - trades a bit of smoothing between frames for staying perfectly in sync with SVC's frame rate
		// (no internal buffering/latency drift).
		this.pitchShifter = new PitchShifter(1.0, SAMPLE_RATE_HZ, FRAME_SIZE, 0);
		this.pitchDetector = new Yin(SAMPLE_RATE_HZ, DETECTION_WINDOW_SIZE);
		TarsosDSPAudioFormat format = new TarsosDSPAudioFormat(SAMPLE_RATE_HZ, 16, 1, true, false);
		this.audioEvent = new AudioEvent(format);
	}

	short[] decode(byte[] encodedData) {
		try {
			return (short[]) decoderMethod.invoke(decoder, (Object) encodedData);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Unable to decode microphone audio with the installed voice-chat API", e);
		}
	}

	byte[] encode(short[] pcm) {
		try {
			return (byte[]) encoderMethod.invoke(encoder, (Object) pcm);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Unable to encode microphone audio with the installed voice-chat API", e);
		}
	}

	private static Object createCodec(VoicechatApi api, String methodName) {
		try {
			Method method = VoicechatApi.class.getMethod(methodName);
			return method.invoke(api);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("The installed voice-chat API does not provide " + methodName + "()", e);
		}
	}

	private static Method findMethod(Object codec, String methodName, Class<?> parameterType) {
		try {
			return codec.getClass().getMethod(methodName, parameterType);
		} catch (NoSuchMethodException e) {
			throw new IllegalStateException("The installed voice-chat API codec object does not provide " + methodName + "(" + parameterType.getSimpleName() + ")", e);
		}
	}

	void close() {
		try {
			decoder.getClass().getMethod("close").invoke(decoder);
			encoder.getClass().getMethod("close").invoke(encoder);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Unable to close voice-chat codec objects", e);
		}
	}
}
