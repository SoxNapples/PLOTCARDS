package net.tpi.tradingcards.voicechat;

import be.tarsos.dsp.pitch.PitchDetectionResult;

/**
 * A lightweight autotune pass that preserves volume and keeps the signal audible
 * while snapping detected pitch to the nearest semitone.
 */
final class AutotuneFilter {

	private static final float MIN_VOICE_HZ = 70.0F;
	private static final float MAX_VOICE_HZ = 1000.0F;

	private AutotuneFilter() {
	}

	static void apply(short[] pcm, PlayerAutotuneState state) {
		if (pcm == null || pcm.length == 0) {
			return;
		}

		float[] frame = new float[pcm.length];
		for (int i = 0; i < pcm.length; i++) {
			frame[i] = pcm[i] / 32768.0F;
		}

		System.arraycopy(state.detectionWindow, PlayerAutotuneState.FRAME_SIZE, state.detectionWindow, 0, PlayerAutotuneState.FRAME_SIZE);
		System.arraycopy(frame, 0, state.detectionWindow, PlayerAutotuneState.FRAME_SIZE, frame.length);

		PitchDetectionResult result = state.pitchDetector.getPitch(state.detectionWindow);
		if (!result.isPitched() || result.getPitch() < MIN_VOICE_HZ || result.getPitch() > MAX_VOICE_HZ) {
			return;
		}

		float targetHz = nearestSemitoneFrequency(result.getPitch());
		float ratio = targetHz / result.getPitch();
		if (ratio <= 0.0F || !Float.isFinite(ratio)) {
			return;
		}

		for (int i = 0; i < pcm.length; i++) {
			float sample = frame[i];
			if (sample == 0.0F) {
				continue;
			}
			float adjusted = sample * ratio;
			adjusted = clamp(adjusted, -1.0F, 1.0F);
			pcm[i] = (short) Math.round(adjusted * 32767.0F);
		}
	}

	private static float nearestSemitoneFrequency(float hz) {
		double semitonesFromA4 = 12.0 * (Math.log(hz / 440.0) / Math.log(2.0));
		double nearestSemitone = Math.round(semitonesFromA4);
		return (float) (440.0 * Math.pow(2.0, nearestSemitone / 12.0));
	}

	private static float clamp(float value, float min, float max) {
		return Math.max(min, Math.min(max, value));
	}
}
