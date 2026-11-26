package com.alpoyy.sneakgrow.core;

/**
 * Perhitungan radius deteksi tanaman (logika murni, tanpa dependensi Minecraft,
 * sehingga bisa di-unit-test langsung).
 *
 * <p><b>Pendekatan radius (dokumentasi desain):</b>
 * <ul>
 *   <li>Radius diukur sebagai jarak Euclidean dari <b>pusat blok tempat kaki
 *       pemain berada</b> ke <b>pusat blok tanaman target</b>.</li>
 *   <li>Pusat blok = koordinat blok + 0.5 pada setiap sumbu.</li>
 *   <li>Blok dianggap di dalam radius bila jarak &lt;= radius (inklusif), sehingga
 *       blok yang tepat berjarak 4.0 blok (misal 4 kolom ke samping, atau 4 blok
 *       ke atas/bawah) tetap terhitung.</li>
 *   <li>Perbandingan memakai kuadrat jarak (tanpa {@code Math.sqrt}) agar murah
 *       dan bebas error pembulatan.</li>
 *   <li>Definisi ini konsisten dan mudah diprediksi: pemain di tengah, tanaman di
 *       samping, atas, dan bawah pemain tercakup sesuai batas radius.</li>
 * </ul>
 */
public final class RadiusCheck {
	private RadiusCheck() {
	}

	/**
	 * Memeriksa apakah blok target berada di dalam radius dari blok tengah (kaki pemain).
	 *
	 * @param centerX koordinat X blok kaki pemain
	 * @param centerY koordinat Y blok kaki pemain
	 * @param centerZ koordinat Z blok kaki pemain
	 * @param targetX koordinat X blok target
	 * @param targetY koordinat Y blok target
	 * @param targetZ koordinat Z blok target
	 * @param radius  radius dalam blok (harus &gt;= 0)
	 * @return true bila pusat blok target berjarak Euclidean &lt;= radius dari pusat blok pemain
	 */
	public static boolean isWithinRadius(int centerX, int centerY, int centerZ,
			int targetX, int targetY, int targetZ, double radius) {
		if (radius < 0.0) {
			return false;
		}

		double dx = (targetX + 0.5) - (centerX + 0.5);
		double dy = (targetY + 0.5) - (centerY + 0.5);
		double dz = (targetZ + 0.5) - (centerZ + 0.5);

		double radiusSquared = radius * radius;
		return dx * dx + dy * dy + dz * dz <= radiusSquared;
	}
}
