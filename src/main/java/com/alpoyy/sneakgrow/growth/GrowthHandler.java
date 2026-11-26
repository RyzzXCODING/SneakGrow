package com.alpoyy.sneakgrow.growth;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.alpoyy.sneakgrow.SneakGrowConfig;
import com.alpoyy.sneakgrow.core.RadiusCheck;
import com.alpoyy.sneakgrow.core.TickGate;
import com.alpoyy.sneakgrow.plant.PlantCategory;
import com.alpoyy.sneakgrow.plant.PlantClassifier;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Jantung SneakGrow: setiap interval (default 20 tick), untuk setiap world, cari
 * pemain yang jongkok, lalu beri <b>satu vanilla random tick</b> ke setiap tanaman
 * valid di dalam radius (default 4 blok) di sekitarnya.
 *
 * <p><b>Mekanisme growth:</b> SneakGrow tidak mengganti/membypass mekanisme vanilla.
 * Memanggil {@code randomTick} milik blok sama persis dengan yang dilakukan engine
 * saat random tick vanilla mengenai blok tersebut — sehingga syarat cahaya,
 * kelembapan farmland, batas usia/tinggi, dan penempatan buah (stem) semuanya
 * tetap ditangani kode vanilla. Tidak ada bone meal yangdikonsumsi, tidak ada
 * inventory yang disentuh, tidak ada item yang di-duplicate.
 *
 * <p><b>Keamanan performa & server:</b>
 * <ul>
 *   <li>Event tick hanya melakukan pengecekan murah; pemindaian penuh berjalan
 *       1x per interval, bukan setiap tick.</li>
 *   <li>Scan dibatasi kubus kecil di sekitar pemain (maksimal (2*8+1)^3 posisi
 *       pada radius config maksimum), tidak pernah memindai seluruh chunk.</li>
 *   <li>Tanaman yang sama hanya diproses satu kali per interval walau radius
 *       beberapa pemain tumpang tindih (dedup via {@link BlockPos#asLong} per world).</li>
 *   <li>Budget maksimum tanaman per pemain per interval sebagai rem kasus terburuk.</li>
 *   <li>Tidak ada state per player, tidak ada thread tambahan, tidak ada timer:
 *       pemain logout atau world unload tidak meninggalkan proses apaun.</li>
 *   <li>Semua perubahan blok terjadi di ServerWorld (authoritative); tidak ada
 *       kode client yang bisa memaksakan growth.</li>
 * </ul>
 */
public final class GrowthHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger("SneakGrow");

	/** Rem performa: maksimum tanaman yang diproses per pemain per interval. */
	private static final int MAX_PLANTS_PER_PLAYER_PER_PASS = 256;

	private final com.alpoyy.sneakgrow.SneakGrowConfig config;
	private final TickGate gate;
	private static final Method RANDOM_TICK_METHOD;

	static {
		try {
			Method method = net.minecraft.block.AbstractBlock.class.getMethod(
					"randomTick",
					net.minecraft.block.BlockState.class,
					net.minecraft.world.World.class,
					net.minecraft.util.math.BlockPos.class,
					java.util.Random.class);
			method.setAccessible(true);
			RANDOM_TICK_METHOD = method;
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Failed to find randomTick method", e);
		}
	}

	public GrowthHandler(com.alpoyy.sneakgrow.SneakGrowConfig config) {
		this.config = config;
		this.gate = new TickGate(config.getIntervalTicks());
	}

	/** Mendaftarkan listener tick server. Dipanggil sekali saat mod init. */
	public void register() {
		ServerTickEvents.END_SERVER_TICK.register(this::onEndServerTick);
	}

	private void onEndServerTick(MinecraftServer server) {
		if (!config.isEnabled()) {
			return;
		}

		if (!gate.shouldRun()) {
			return;
		}

		for (ServerWorld world : server.getWorlds()) {
			processWorld(world);
		}
	}

	private void processWorld(ServerWorld world) {
		List<ServerPlayerEntity> sneakingPlayers = world.getPlayers().stream()
				.filter(GrowthHandler::isEligible)
				.toList();

		if (sneakingPlayers.isEmpty()) {
			return;
		}

		Set<Long> processedThisPass = new HashSet<>();
		for (ServerPlayerEntity player : sneakingPlayers) {
			processAroundPlayer(world, player, processedThisPass);
		}
	}

	/**
	 * Pemain dianggap memenuhi syarat bila hidup, bukan spectator, dan sedang
	 * jongkok (flag sneak aktif). Efek aktif selama flag dipertahankan — bukan
	 * hanya sekali saat Shift ditekan — dan berhenti otomatis saat berdiri karena
	 * pemain tidak lagi lolos filter ini.
	 */
	private static boolean isEligible(ServerPlayerEntity player) {
		return player.isAlive() && !player.isSpectator() && player.isSneaking();
	}

	private void processAroundPlayer(ServerWorld world, ServerPlayerEntity player, Set<Long> processedThisPass) {
		BlockPos center = player.getBlockPos();
		int radius = config.getRadius();
		int budget = MAX_PLANTS_PER_PLAYER_PER_PASS;

		for (int dy = -radius; dy <= radius; dy++) {
			for (int dz = -radius; dz <= radius; dz++) {
				for (int dx = -radius; dx <= radius; dx++) {
					if (budget <= 0) {
						return;
					}

					int x = center.getX() + dx;
					int y = center.getY() + dy;
					int z = center.getZ() + dz;

					if (!RadiusCheck.isWithinRadius(center.getX(), center.getY(), center.getZ(), x, y, z, radius)) {
						continue;
					}

					BlockPos pos = new BlockPos(x, y, z);
					BlockState state = world.getBlockState(pos);
					Block block = state.getBlock();

					PlantCategory category = PlantClassifier.classify(block);
					if (category == PlantCategory.IGNORED || category == PlantCategory.BLACKLISTED) {
						continue;
					}

					// Tanaman sama hanya diproses satu kali per interval, walau radius
					// beberapa pemain tumpang tindih.
					if (!processedThisPass.add(pos.asLong())) {
						continue;
					}

					budget--;
					applyGrowth(world, pos, state, block, category);
				}
			}
		}
	}

	/**
	 * Apply satu vanilla random tick ke blok. Menggunakan refleksi untuk
	 * mengatasi akses protected pada AbstractBlock.randomTick().
	 */
	private void applyGrowth(ServerWorld world, BlockPos pos, BlockState state, Block block, PlantCategory category) {
		if (category == PlantCategory.SUGAR_CANE) {
			if (!config.isSugarCaneEnabled()) {
				return;
			}
			SugarCaneHandler.tryGrow(world, pos, state, (SugarCaneBlock) block);
			return;
		}

		// CROP dan RANDOM_TICK_PLANT: satu vanilla random tick. Mekanisme growth,
		// syarat cahaya/kelembapan, dan batas usia ditangani kode vanilla sendiri.
		if (!state.canPlaceAt(world, pos)) {
			// Jangan memaksa growth bila kondisi penanaman vanilla tidak terpenuhi.
			return;
		}

		try {
			RANDOM_TICK_METHOD.invoke(null, state, world, pos, world.random);
		} catch (InvocationTargetException | IllegalAccessException e) {
			LOGGER.warn("[SneakGrow] Gagal memulai random tick untuk {} di {}: {}", block, pos, e.toString());
		}
	}
}