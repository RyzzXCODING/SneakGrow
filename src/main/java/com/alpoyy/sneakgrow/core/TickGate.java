package com.alpoyy.sneakgrow.core;

/**
 * Gerbang interval: memastikan growth hanya diproses satu kali setiap
 * {@code intervalTicks} tick server, bukan setiap tick.
 *
 * <p>Satu instance dipakai untuk seluruh server (state minimal: dua field angka),
 * sehingga tidak ada state per player yang perlu dibersihkan saat pemain logout
 * atau world unload.
 */
public final class TickGate {
	private final int intervalTicks;

	private long ticksSinceLastRun;
	private boolean firstRun = true;

	public TickGate(int intervalTicks) {
		this.intervalTicks = Math.max(1, intervalTicks);
	}

	/**
	 * Dipanggil tepat satu kali per tick server.
	 *
	 * @return true bila interval penuh sudah tercapai dan pemrosesan boleh berjalan
	 */
	public boolean shouldRun() {
		if (firstRun) {
			firstRun = false;
			return true;
		}

		if (++ticksSinceLastRun >= intervalTicks) {
			ticksSinceLastRun = 0;
			return true;
		}

		return false;
	}

	public int getIntervalTicks() {
		return intervalTicks;
	}
}
