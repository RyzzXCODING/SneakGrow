package com.alpoyy.sneakgrow;

import java.nio.file.Path;

import com.alpoyy.sneakgrow.growth.GrowthHandler;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SneakGrow — mempercepat pertumbuhan tanaman di sekitar pemain saat jongkok.
 *
 * <p>Mekanisme aktivasi: growth dipicu berdasarkan transisi state crouch pemain.
 * Ketika player mulai jongkok (transisi dari berdiri ke jongkok), growth diproses
 * instan. Setiap siklus crouch→stand→crouch berikutnya bisa memicu growth lagi.
 * Menahan crouch terus memproses growth secara kontinu.
 *
 * <p>Seluruh logika berjalan di sisi server (authoritative): deteksi sneak,
 * pemindaian radius, dan perubahan blok semua terjadi pada thread server.
 * Tidak ada kode client dan tidak ada networking khusus, sehingga mod cukup
 * terpasang di server dedicated; untuk singleplayer, mod terpasang di klien
 * dan logikanya berjalan pada integrated server.
 */
public class SneakGrow implements ModInitializer {
	public static final String MOD_ID = "sneakgrow";
	public static final Logger LOGGER = LoggerFactory.getLogger("SneakGrow");

	// State per-player untuk tracking crouch activation
	public static final java.util.Map<Long, Boolean> SNEAKING_STATE = new java.util.HashMap<>();

	@Override
	public void onInitialize() {
		Path configPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");
		SneakGrowConfig config = SneakGrowConfig.load(configPath);

		new GrowthHandler(config).register();

		LOGGER.info(
				"[SneakGrow] Aktif. Radius {} blok, percepatan sugar cane: {}. "
						+ "Jongkok (Shift) di dekat tanaman untuk mempercepat pertumbuhan.",
				config.getRadius(),
				config.isSugarCaneEnabled() ? "aktif" : "nonaktif");
	}
}