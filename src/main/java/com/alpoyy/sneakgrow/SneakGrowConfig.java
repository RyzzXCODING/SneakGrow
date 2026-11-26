package com.alpoyy.sneakgrow;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Config sederhana berbasis JSON ({@code config/sneakgrow.json}).
 *
 * <p>Desain dengan sengaja sederhana dan stabil:
 * <ul>
 *   <li>File dibuat otomatis dengan nilai default saat pertama kali server start.</li>
 *   <li>File rusak/tidak valid TIDAK pernah melempar exception ke loader —
 *       nilai default dipakai dan diberi peringatan di log.</li>
 *   <li>Nilai di luar rentang aman di-clamp, tidak pernah membuat mod crash.</li>
 *   <li>Perubahan config berlaku setelah server restart (dibaca sekali saat init).</li>
 * </ul>
 *
 * <p>Field dipakai tipe boxed agar nilai yang hilang di JSON bisa dibedakan dari
 * nilai {@code false}/{@code 0} dan diganti default, bukan membisu.
 */
public final class SneakGrowConfig {
	public static final Logger LOGGER = LoggerFactory.getLogger("SneakGrow");

	public static final int DEFAULT_RADIUS = 4;
	public static final int MIN_RADIUS = 1;
	public static final int MAX_RADIUS = 8;

	public static final int DEFAULT_INTERVAL_TICKS = 20;
	public static final int MIN_INTERVAL_TICKS = 5;
	public static final int MAX_INTERVAL_TICKS = 200;

	public static final boolean DEFAULT_ENABLED = true;
	public static final boolean DEFAULT_SUGAR_CANE_ENABLED = true;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	/** Radius deteksi tanaman dalam blok (default 4). */
	public Integer radius;
	/** Jarak antar pemrosesan growth dalam tick server (default 20 tick = 1 detik). */
	public Integer intervalTicks;
	/** Aktif/nonaktifkan mod secara keseluruhan. */
	public Boolean enabled;
	/** Aktif/nonaktifkan percepatan khusus sugar cane. */
	public Boolean sugarCaneEnabled;

	public int getRadius() {
		return radius;
	}

	public int getIntervalTicks() {
		return intervalTicks;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isSugarCaneEnabled() {
		return sugarCaneEnabled;
	}

	/**
	 * Mengunci seluruh nilai ke rentang aman dan mengisi nilai kosong dengan default.
	 *
	 * @return this, agar bisa di-chain.
	 */
	public SneakGrowConfig sanitize() {
		radius = clamp(radius, MIN_RADIUS, MAX_RADIUS, DEFAULT_RADIUS);
		intervalTicks = clamp(intervalTicks, MIN_INTERVAL_TICKS, MAX_INTERVAL_TICKS, DEFAULT_INTERVAL_TICKS);
		if (enabled == null) {
			enabled = DEFAULT_ENABLED;
		}
		if (sugarCaneEnabled == null) {
			sugarCaneEnabled = DEFAULT_SUGAR_CANE_ENABLED;
		}
		return this;
	}

	private static int clamp(Integer value, int min, int max, int fallback) {
		int v = value == null ? fallback : value;
		return Math.max(min, Math.min(max, v));
	}

	/**
	 * Memuat config dari file. Tidak pernah gagal: bila file tidak ada, default
	 * ditulis; bila file rusak, default dipakai.
	 */
	public static SneakGrowConfig load(Path path) {
		SneakGrowConfig config = new SneakGrowConfig();

		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				SneakGrowConfig parsed = GSON.fromJson(reader, SneakGrowConfig.class);
				if (parsed != null) {
					config = parsed;
				} else {
					LOGGER.warn("[SneakGrow] Config di {} kosong, memakai nilai default.", path);
				}
			} catch (Exception e) {
				LOGGER.warn("[SneakGrow] Gagal membaca config di {} ({}). Memakai nilai default.", path, e.toString());
			}
		} else {
			LOGGER.info("[SneakGrow] Config tidak ditemukan, membuat file default di {}.", path);
			config.sanitize().save(path);
		}

		return config.sanitize();
	}

	/** Menyimpan config ke file (best-effort; kegagalan hanya dicatat di log). */
	public void save(Path path) {
		try {
			if (path.getParent() != null) {
				Files.createDirectories(path.getParent());
			}
			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException e) {
			LOGGER.warn("[SneakGrow] Gagal menyimpan config di {}: {}", path, e.toString());
		}
	}
}
