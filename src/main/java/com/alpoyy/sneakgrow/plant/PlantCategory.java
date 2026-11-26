package com.alpoyy.sneakgrow.plant;

/**
 * Kategori tanaman hasil klasifikasi.
 *
 * <p>Klasifikasi dilakukan berbasis <b>kelas block</b> (bukan nama block), sehingga
 * varian dan alias vanilla yang berbagi kelas tetap terdeteksi dengan benar.
 */
public enum PlantCategory {
	/**
	 * Crop vanilla yang tumbuh lewat random tick: wheat, carrots, potatoes,
	 * beetroot, dan varian lain yang berbagi kelas {@code CropBlock}
	 * (mis. torchflower crop, pitcher crop, pumpkin/melon stem turunannya).
	 */
	CROP,

	/**
	 * Sugar cane — bone meal vanilla tidak bekerja pada sugar cane, jadi
	 * dipercepat lewat mekanisme usia vanilla miliknya sendiri.
	 */
	SUGAR_CANE,

	/**
	 * Tanaman lain dengan mekanisme pertumbuhan vanilla yang valid dan aman
	 * dipicu lewat satu vanilla random tick.
	 */
	RANDOM_TICK_PLANT,

	/**
	 * Vegetasi dekoratif yang sengaja dikecualikan: rumput, tall grass, fern,
	 * large fern, bunga, dead bush. Tidak boleh pernah diproses.
	 */
	BLACKLISTED,

	/**
	 * Blok apa pun yang bukan target (default-deny): tidak diproses sama sekali.
	 * Prinsip keamanan: jika ragu, jangan proses.
	 */
	IGNORED
}
