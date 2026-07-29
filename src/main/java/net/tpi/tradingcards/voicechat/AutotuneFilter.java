package net.tpi.tradingcards.voicechat;

import be.tarsos.dsp.pitch.PitchDetectionResult;

/**
 * Classic "hard-snap" chromatic autotune: detects the speaker's current
 * pitch and instantly corrects it to the nearest equal-tempered semitone (no
 * fixed musical key - any of the 12 notes counts), the same recognizable
 * effect most people mean by "autotune."
 */
final class AutotuneFilter {

	/** Below this, treat whatever Yin thinks it heard as noise/rumble, not a voiced note. */
	private static final float MIN_VOICE_HZ = 70.0F;
	/** Above this, treat it as a detection error (sibilance, breath noise), not a real pitch. */
	private static final float MAX_VOICE_HZ = 1000.0F;
	/** Caps how far a single frame's correction can shift the pitch, so a bad detection can't produce a jarring octave jump. */
	private static final float MAX_SHIFT_RATIO = 2.0F;
	private static final float MIN_SHIFT_RATIO = 1.0F / MAX_SHIFT_RATIO;

	private AutotuneFilter() {
	}

	/** Mutates {@code pcm} in place. {@code state} must be reused across every frame from the same speaker - both the Opus codec and the pitch shifter carry history between frames. */
	static void apply(short[] pcm, PlayerAutotuneState state) {
		float[] frame = new float[pcm.length];
		for (int i = 0; i < pcm.length; i++) {
			frame[i] = pcm[i] / 32768.0F;
		}

		System.arraycopy(state.detectionWindow, PlayerAutotuneState.FRAME_SIZE, state.detectionWindow, 0, PlayerAutotuneState.FRAME_SIZE);
		System.arraycopy(frame, 0, state.detectionWindow, PlayerAutotuneState.FRAME_SIZE, frame.length);

		PitchDetectionResult result = state.pitchDetector.getPitch(state.detectionWindow);
		float ratio = 1.0F;
		if (result.isPitched() && result.getPitch() >= MIN_VOICE_HZ && result.getPitch() <= MAX_VOICE_HZ) {
			float targetHz = nearestSemitoneFrequency(result.getPitch());
			ratio = clamp(targetHz / result.getPitch(), MIN_SHIFT_RATIO, MAX_SHIFT_RATIO);
		}

		state.pitchShifter.setPitchShiftFactor(ratio);
		state.audioEvent.setFloatBuffer(frame);
		state.pitchShifter.process(state.audioEvent);
		float[] shifted = state.audioEvent.getFloatBuffer();

		for (int i = 0; i < pcm.length; i++) {
			float sample = clamp(shifted[i], -1.0F, 1.0F) * 32767.0F;
			pcm[i] = (short) Math.round(sample);
		}
	}

	/** Nearest 12-tone-equal-temperament note to {@code hz}, using A4=440Hz as the reference pitch. */
	private static float nearestSemitoneFrequency(float hz) {
		double semitonesFromA4 = 12.0 * (Math.log(hz / 440.0) / Math.log(2.0));
		double nearestSemitone = Math.round(semitonesFromA4);
		return (float) (440.0 * Math.pow(2.0, nearestSemitone / 12.0));
	}

	private static float clamp(float value, float min, float max) {
		return Math.max(min, Math.min(max, value));
	}
}
