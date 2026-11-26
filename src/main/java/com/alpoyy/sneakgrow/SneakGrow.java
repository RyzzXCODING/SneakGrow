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
 * <p>Seluruh logika berjalan di sisi server (authoritative): deteksi sneak,
 * pemindaian radius, dan perubahan blok semua terjadi pada thread server.
 * Tidak ada kode client dan tidak ada networking khusus, sehingga mod cukup
 * terpasang di server dedicated; untuk singleplayer, mod terpasang di klien
 * dan logikanya berjalan pada integrated server.
 */
public class SneakGrow implements ModInitializer {
	public static final String MOD_ID = "sneakgrow";
	public static final Logger LOGGER = LoggerFactory.getLogger("SneakGrow");

	@Override
	public void onInitialize() {
		Path configPath = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");
		SneakGrowConfig config = SneakGrowConfig.load(configPath);

		new GrowthHandler(config).register();

		LOGGER.info(
				"[SneakGrow] Aktif. Radius {} blok, interval {} tick, percepatan sugar cane: {}. "
						+ "Jongkok (Shift) di dekat tanaman untuk mempercepat pertumbuhan.",
				config.getRadius(),
				config.getIntervalTicks(),
				config.isSugarCaneEnabled() ? "aktif" : "nonaktif");
	}
}
