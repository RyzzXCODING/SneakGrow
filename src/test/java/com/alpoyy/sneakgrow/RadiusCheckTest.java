package com.alpoyy.sneakgrow;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.alpoyy.sneakgrow.core.RadiusCheck;

/** Pengujian radius detection dan boundary radius (default 4 blok). */
class RadiusCheckTest {
	private static final double R = 4.0;

	@Test
	void blokPemainSendiriTermasuk() {
		assertTrue(RadiusCheck.isWithinRadius(0, 64, 0, 0, 64, 0, R));
	}

	@Test
	void blokBertetanggaTermasuk() {
		assertTrue(RadiusCheck.isWithinRadius(0, 64, 0, 1, 64, 0, R));
		assertTrue(RadiusCheck.isWithinRadius(0, 64, 0, -1, 64, 0, R));
		assertTrue(RadiusCheck.isWithinRadius(0, 64, 0, 0, 65, 0, R));
		assertTrue(RadiusCheck.isWithinRadius(0, 64, 0, 0, 63, 0, R));
	}

	@Test
	void batasTepatEmpatBlokTermasukInklusif() {
		// Pusat blok target tepat 4.0 dari pusat blok pemain: 4 samping, 4 atas/bawah.
		assertTrue(RadiusCheck.isWithinRadius(0, 64, 0, 4, 64, 0, R), "4 kolom ke timur");
		assertTrue(RadiusCheck.isWithinRadius(0, 64, 0, -4, 64, 0, R), "4 kolom ke barat");
		assertTrue(RadiusCheck.isWithinRadius(0, 64, 0, 0, 64, 4, R), "4 kolom ke selatan");
		assertTrue(RadiusCheck.isWithinRadius(0, 64, 0, 0, 64, -4, R), "4 kolom ke utara");
		assertTrue(RadiusCheck.isWithinRadius(0, 64, 0, 0, 68, 0, R), "4 blok ke atas");
		assertTrue(RadiusCheck.isWithinRadius(0, 64, 0, 0, 60, 0, R), "4 blok ke bawah");
	}

	@Test
	void satuBlokDiLuarBatasDikeluarkan() {
		assertFalse(RadiusCheck.isWithinRadius(0, 64, 0, 5, 64, 0, R), "5 kolom ke timur");
		assertFalse(RadiusCheck.isWithinRadius(0, 64, 0, 0, 69, 0, R), "5 blok ke atas");
		assertFalse(RadiusCheck.isWithinRadius(0, 64, 0, 0, 59, 0, R), "5 blok ke bawah");
	}

	@Test
	void diagonalJauhDikeluarkan() {
		// sqrt(16 + 1) ~= 4.12 > 4
		assertFalse(RadiusCheck.isWithinRadius(0, 64, 0, 4, 65, 0, R));
		// sqrt(9 + 9) ~= 4.24 > 4
		assertFalse(RadiusCheck.isWithinRadius(0, 64, 0, 3, 64, 3, R));
		// sqrt(16 + 16) ~= 5.66 > 4
		assertFalse(RadiusCheck.isWithinRadius(0, 64, 0, 4, 64, 4, R));
		// sqrt(9 + 1 + 9) ~= 4.36 > 4
		assertFalse(RadiusCheck.isWithinRadius(0, 64, 0, 3, 65, 3, R));
	}

	@Test
	void diagonalDalamRadiusTermasuk() {
		// sqrt(4 + 1 + 4) = 3.0 <= 4
		assertTrue(RadiusCheck.isWithinRadius(0, 64, 0, 2, 65, 2, R));
		// sqrt(9 + 4) ~= 3.61 <= 4
		assertTrue(RadiusCheck.isWithinRadius(0, 64, 0, 3, 64, 2, R));
		// sqrt(4 + 4) ~= 2.83 <= 4
		assertTrue(RadiusCheck.isWithinRadius(0, 64, 0, 2, 64, 2, R));
	}

	@Test
	void radiusKustomKonsisten() {
		// Radius 2: blok 2 kolom termasuk, 3 kolom tidak.
		assertTrue(RadiusCheck.isWithinRadius(0, 64, 0, 2, 64, 0, 2.0));
		assertFalse(RadiusCheck.isWithinRadius(0, 64, 0, 3, 64, 0, 2.0));
	}

	@Test
	void radiusNolHanyaBlokPemain() {
		assertTrue(RadiusCheck.isWithinRadius(0, 64, 0, 0, 64, 0, 0.0));
		assertFalse(RadiusCheck.isWithinRadius(0, 64, 0, 1, 64, 0, 0.0));
	}

	@Test
	void radiusNegatifSelaluDitolak() {
		assertFalse(RadiusCheck.isWithinRadius(0, 64, 0, 0, 64, 0, -1.0));
	}
}
