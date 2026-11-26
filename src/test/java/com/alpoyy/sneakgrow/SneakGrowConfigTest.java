package com.alpoyy.sneakgrow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Pengujian config: default, validasi/clamp, dan ketahanan terhadap file rusak. */
class SneakGrowConfigTest {
	@TempDir
	Path tempDir;

	@Test
	void defaultSesuaiSpesifikasi() {
		SneakGrowConfig config = new SneakGrowConfig().sanitize();

		assertEquals(4, config.getRadius(), "radius default harus 4 blok");
		assertEquals(20, config.getIntervalTicks(), "interval default 20 tick = 1 detik");
		assertTrue(config.isEnabled());
		assertTrue(config.isSugarCaneEnabled());
	}

	@Test
	void nilaiDiLuarBatasDiClamp() {
		SneakGrowConfig config = new SneakGrowConfig();
		config.radius = 999;
		config.intervalTicks = -10;
		config.sanitize();

		assertEquals(SneakGrowConfig.MAX_RADIUS, config.getRadius());
		assertEquals(SneakGrowConfig.MIN_INTERVAL_TICKS, config.getIntervalTicks());
	}

	@Test
	void nilaiTerlaluKecilDiClampKeBatasBawah() {
		SneakGrowConfig config = new SneakGrowConfig();
		config.radius = 0;
		config.intervalTicks = 1;
		config.sanitize();

		assertEquals(SneakGrowConfig.MIN_RADIUS, config.getRadius());
		assertEquals(SneakGrowConfig.MIN_INTERVAL_TICKS, config.getIntervalTicks());
	}

	@Test
	void fieldNullDigantiDefault() {
		SneakGrowConfig config = new SneakGrowConfig();
		config.radius = null;
		config.enabled = null;
		config.sugarCaneEnabled = null;
		config.sanitize();

		assertEquals(SneakGrowConfig.DEFAULT_RADIUS, config.getRadius());
		assertTrue(config.isEnabled());
		assertTrue(config.isSugarCaneEnabled());
	}

	@Test
	void fileTidakAdaMembuatDefaultDanFile() throws IOException {
		Path path = tempDir.resolve("sneakgrow.json");

		SneakGrowConfig config = SneakGrowConfig.load(path);

		assertEquals(4, config.getRadius());
		assertTrue(Files.exists(path), "config default harus otomatis dibuat");
	}

	@Test
	void roundTripJsonDibacaKembali() throws IOException {
		Path path = tempDir.resolve("sneakgrow.json");

		SneakGrowConfig original = new SneakGrowConfig().sanitize();
		original.radius = 6;
		original.sugarCaneEnabled = false;
		original.save(path);

		SneakGrowConfig loaded = SneakGrowConfig.load(path);

		assertEquals(6, loaded.getRadius());
		assertEquals(20, loaded.getIntervalTicks());
		assertFalse(loaded.isSugarCaneEnabled());
		assertTrue(loaded.isEnabled());
	}

	@Test
	void jsonRusakMemakaiDefaultTanpaCrash() throws IOException {
		Path path = tempDir.resolve("sneakgrow.json");
		Files.writeString(path, "{ ini bukan json ]]]");

		SneakGrowConfig config = SneakGrowConfig.load(path);

		assertEquals(4, config.getRadius());
		assertEquals(20, config.getIntervalTicks());
		assertTrue(config.isEnabled());
	}

	@Test
	void jsonKosongMemakaiDefault() throws IOException {
		Path path = tempDir.resolve("sneakgrow.json");
		Files.writeString(path, "{}");

		SneakGrowConfig config = SneakGrowConfig.load(path);

		assertEquals(4, config.getRadius());
		assertTrue(config.isEnabled());
		assertTrue(config.isSugarCaneEnabled());
	}

	@Test
	void jsonValidDiclampSaatDibaca() throws IOException {
		Path path = tempDir.resolve("sneakgrow.json");
		Files.writeString(path,
				"{\"radius\": 500, \"intervalTicks\": 1, \"enabled\": true, \"sugarCaneEnabled\": true}");

		SneakGrowConfig config = SneakGrowConfig.load(path);

		assertEquals(SneakGrowConfig.MAX_RADIUS, config.getRadius());
		assertEquals(SneakGrowConfig.MIN_INTERVAL_TICKS, config.getIntervalTicks());
	}
}
