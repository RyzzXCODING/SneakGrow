package com.alpoyy.sneakgrow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.alpoyy.sneakgrow.core.TickGate;

/**
 * Pengujian cooldown/interval pemrosesan: growth tidak boleh berjalan setiap tick,
 * dan harus berjalan tepat sesuai interval.
 */
class TickGateTest {
	@Test
	void intervalTigaBerjalanTepatSetiapTigaTick() {
		TickGate gate = new TickGate(3);

		// Tick pertama server: langsung berjalan sekali (tidak menunggu interval penuh).
		assertTrue(gate.shouldRun());
		// Dua tick berikutnya: tidak berjalan.
		assertFalse(gate.shouldRun());
		assertFalse(gate.shouldRun());
		// Tick ke-4 sejak mulai: interval penuh, berjalan lagi.
		assertTrue(gate.shouldRun());
		assertFalse(gate.shouldRun());
		assertFalse(gate.shouldRun());
		assertTrue(gate.shouldRun());
	}

	@Test
	void intervalSatuBerjalanSetiapTick() {
		TickGate gate = new TickGate(1);

		assertTrue(gate.shouldRun());
		assertTrue(gate.shouldRun());
		assertTrue(gate.shouldRun());
	}

	@Test
	void intervalTidakValidDikunciKeSatu() {
		assertEquals(1, new TickGate(0).getIntervalTicks());
		assertEquals(1, new TickGate(-5).getIntervalTicks());
	}

	@Test
	void intervalDuaPuluhTidakMemprosesSetiapTick() {
		TickGate gate = new TickGate(20);
		int runs = 0;

		for (int i = 0; i < 200; i++) {
			if (gate.shouldRun()) {
				runs++;
			}
		}

		// 1x di tick pertama + sisa 199 tick dibagi 20 (pembagian bulat) = 10.
		assertEquals(1 + 199 / 20, runs);
	}

	@Test
	void polaKonstanSetelahTickPertama() {
		TickGate gate = new TickGate(5);

		// Lewati tick pertama.
		assertTrue(gate.shouldRun());

		// 25 tick berikutnya harus tepat 5 kali berjalan (tiap 5 tick).
		int runs = 0;
		for (int i = 0; i < 25; i++) {
			if (gate.shouldRun()) {
				runs++;
			}
		}
		assertEquals(5, runs);
	}
}
