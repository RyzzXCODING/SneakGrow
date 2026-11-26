package com.alpoyy.sneakgrow.growth;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;
import net.minecraft.block.BlockState;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mekanisme khusus sugar cane (fitur utama SneakGrow).
 *
 * <p>Vanilla <b>tidak</b> mendukung bone meal pada sugar cane, sehingga jalur
 * fertilization tidak bisa dipakai. SneakGrow tidak memaksa growth sendiri;
 * sebagai gantinya, setiap pemrosesan memanggil <b>satu vanilla random tick</b>
 * milik blok itu sendiri ({@code SugarCaneBlock#randomTick}). Semua aturan vanilla
 * tetap berlaku penuh dan tidak ada yang di-bypass:
 *
 * <ul>
 *   <li>Pertumbuhan hanya terjadi bila blok di atas stalk kosong (air).</li>
 *   <li>Sugar cane punya penghitung usia vanilla 0–15; stalk baru hanya muncul
 *       saat usia penuh. Percepatan ini setara menaikkan frekuensi random tick,
 *       bukan pertumbuhan instan.</li>
 *   <li>Batas tinggi vanilla (maksimum 3 blok) ditegakkan oleh kode vanilla
 *       itu sendiri — SneakGrow tidak menyentuh batas ini.</li>
 *   <li>Soil dan sumber air tidak pernah diubah; stalk tidak pernah dihancurkan;
 *       tidak ada duplikasi item.</li>
 *   <li>{@code canPlaceAt} tetap diperiksa lebih dulu: tebu yang penempatannya
 *       tidak valid (tanah/ground tidak sesuai vanilla) tidak diproses.</li>
 * </ul>
 */
public final class SugarCaneHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger("SneakGrow");

	private static final Method RANDOM_TICK_METHOD;

	static {
		try {
			Method method = net.minecraft.block.SugarCaneBlock.class.getMethod(
					"randomTick",
					BlockState.class,
					ServerWorld.class,
					BlockPos.class,
					Random.class);
			method.setAccessible(true);
			RANDOM_TICK_METHOD = method;
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Failed to find SugarCaneBlock.randomTick method", e);
		}
	}

	private SugarCaneHandler() {
	}

	/**
	 * Memberikan satu vanilla random tick pada sugar cane bila syarat vanilla terpenuhi.
	 *
	 * @param world world server
	 * @param pos   posisi blok sugar cane
	 * @param state state blok sugar cane
	 * @param cane  blok sugar cane
	 */
	public static void tryGrow(ServerWorld world, BlockPos pos, BlockState state, SugarCaneBlock cane) {
		// Pertahankan syarat penanaman vanilla (soil/ground valid).
		if (!state.canPlaceAt(world, pos)) {
			return;
		}

		// Satu vanilla random tick: SugarCaneBlock menaikkan usianya dan menumbuhkan
		// stalk baru sendiri sesuai seluruh aturan vanilla (termasuk batas tinggi 3).
		try {
			RANDOM_TICK_METHOD.invoke(null, state, world, pos, world.random);
		} catch (InvocationTargetException | IllegalAccessException e) {
			LOGGER.warn("[SneakGrow] Gagal memulai random tick pada sugar cane di {}: {}", pos, e.toString());
		}
	}
}