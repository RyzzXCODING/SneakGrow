package com.alpoyy.sneakgrow.plant;

import net.minecraft.block.Block;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.ChorusFlowerBlock;
import net.minecraft.block.CocoaBlock;
import net.minecraft.block.CropBlock;

import net.minecraft.block.FlowerBlock;
import net.minecraft.block.NetherWartBlock;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.ShortPlantBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.block.TallPlantBlock;

/**
 * Deteksi tanaman: whitelist berbasis kelas + blacklist defensif + default-deny.
 *
 * <p><b>Whitelist</b> — hanya kelas-kelas berikut yang diproses. Semuanya memiliki
 * mekanisme pertumbuhan vanilla yang valid dan dianalisis kompatibel dengan
 * pemicuan satu vanilla random tick:
 * <ul>
 *   <li>{@link CropBlock} — wheat, carrots, potatoes, beetroot (termasuk varian
 *       seperti torchflower crop dan pitcher crop yang mewarisi kelas ini).</li>
 *   <li>{@link SugarCaneBlock} — dipercepat lewat penghitung usia vanilla.</li>
 *   <li>{@link BambooBlock}, {@link CactusBlock} — punya batas tinggi vanilla sendiri.</li>
 *   <li>{@link NetherWartBlock}, {@link CocoaBlock} — bone meal vanilla tidak
 *       berpengaruh; growth lewat random tick usia.</li>
 *   <li>{@link StemBlock} — pumpkin/melon stem.</li>
 *   <li>{@link SaplingBlock} — bibit pohon.</li>
 *   <li>{@link SweetBerryBushBlock} — sweet berry bush.</li>
 *   <li>{@link ChorusFlowerBlock} — penggerak pertumbuhan chorus plant.</li>
 * </ul>
 *
 * <p><b>Blacklist</b> — vegetasi dekoratif dilarang eksplisit dan tidak akan pernah
 * diproses walau berada di dalam radius:
 * <ul>
 *   <li>{@link ShortPlantBlock} — grass dan fern.</li>
 *   <li>{@link TallPlantBlock} — tall grass, large fern, dan bunga 2 blok
 *       (sunflower, lilac, rose bush, peony).</li>
 *   <li>{@link FlowerBlock} — semua bunga kecil (dandelion, poppy, wither rose, dst).</li>
 *   <li>{@link DeadBushBlock} — dead bush.</li>
 * </ul>
 *
 * <p><b>Default-deny</b> — semua blok yang tidak dikenali (termasuk seagrass, kelp,
 * azalea, blok mod lain) masuk kategori {@link PlantCategory#IGNORED} dan tidak
 * diproses. Prinsipnya: jika kategori blok tidak jelas, jangan proses.
 */
public final class PlantClassifier {
	private PlantClassifier() {
	}

	/**
	 * Mengklasifikasikan sebuah block.
	 *
	 * @param block block yang ingin diklasifikasikan (tidak boleh null)
	 * @return kategori tanaman; tidak pernah null
	 */
	public static PlantCategory classify(Block block) {
		if (isBlacklistedDecorative(block)) {
			return PlantCategory.BLACKLISTED;
		}

		if (block instanceof CropBlock) {
			return PlantCategory.CROP;
		}

		if (block instanceof SugarCaneBlock) {
			return PlantCategory.SUGAR_CANE;
		}

		if (block instanceof BambooBlock
				|| block instanceof CactusBlock
				|| block instanceof NetherWartBlock
				|| block instanceof CocoaBlock
				|| block instanceof StemBlock
				|| block instanceof SaplingBlock
				|| block instanceof SweetBerryBushBlock
				|| block instanceof ChorusFlowerBlock) {
			return PlantCategory.RANDOM_TICK_PLANT;
		}

		return PlantCategory.IGNORED;
	}

	/**
	 * Blacklist dekoratif: diperiksa lebih dulu sehingga walau suatu saat ada
	 * kelas dekoratif yang mewarisi kelas crop, ia tetap tidak akan diproses.
	 */
	private static boolean isBlacklistedDecorative(Block block) {
		return block instanceof ShortPlantBlock
				|| block instanceof TallPlantBlock
				|| block instanceof FlowerBlock;
	}
}
