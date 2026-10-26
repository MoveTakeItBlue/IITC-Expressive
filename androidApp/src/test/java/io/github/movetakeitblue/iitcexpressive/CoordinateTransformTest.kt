package io.github.movetakeitblue.iitcexpressive

import io.github.movetakeitblue.iitcexpressive.util.CoordinateTransform
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class CoordinateTransformTest {

    @Test
    fun testOutOfChina() {
        // New York (USA)
        assertTrue(CoordinateTransform.outOfChina(40.7128, -74.0060))
        // London (UK)
        assertTrue(CoordinateTransform.outOfChina(51.5074, -0.1278))
        // Tokyo (Japan)
        assertTrue(CoordinateTransform.outOfChina(35.6762, 139.6503))

        // Beijing (China)
        assertFalse(CoordinateTransform.outOfChina(39.9042, 116.4074))
        // Shanghai (China)
        assertFalse(CoordinateTransform.outOfChina(31.2304, 121.4737))
        // Guangzhou (China)
        assertFalse(CoordinateTransform.outOfChina(23.1291, 113.2644))
    }

    @Test
    fun testOutsideChinaNoShift() {
        val nyLat = 40.7128
        val nyLng = -74.0060
        val (gcjLat, gcjLng) = CoordinateTransform.wgs84ToGcj02(nyLat, nyLng)
        assertEquals(nyLat, gcjLat, 0.0000001)
        assertEquals(nyLng, gcjLng, 0.0000001)

        val (wgsLat, wgsLng) = CoordinateTransform.gcj02ToWgs84(gcjLat, gcjLng)
        assertEquals(nyLat, wgsLat, 0.0000001)
        assertEquals(nyLng, wgsLng, 0.0000001)
    }

    @Test
    fun testBeijingWgs84ToGcj02AndBack() {
        // Tiananmen Square
        val origLat = 39.908722
        val origLng = 116.397496

        val (gcjLat, gcjLng) = CoordinateTransform.wgs84ToGcj02(origLat, origLng)

        // In Beijing, GCJ-02 offset is typically ~100m to 500m
        val latDiff = abs(gcjLat - origLat)
        val lngDiff = abs(gcjLng - origLng)
        assertTrue("GCJ-02 should produce non-zero offset in China", latDiff > 0.0001)
        assertTrue("GCJ-02 should produce non-zero offset in China", lngDiff > 0.0001)

        // Reverse conversion
        val (restoredLat, restoredLng) = CoordinateTransform.gcj02ToWgs84(gcjLat, gcjLng)

        // Verify restoration accuracy within 0.1 mm (1e-6 degrees ~ 0.1m, 1e-8 degrees ~ 1mm)
        assertEquals("Latitude should be restored accurately", origLat, restoredLat, 0.000001)
        assertEquals("Longitude should be restored accurately", origLng, restoredLng, 0.000001)
    }

    @Test
    fun testMultipleCitiesInChina() {
        val testLocations = listOf(
            Pair(31.2304, 121.4737), // Shanghai People's Square
            Pair(23.1291, 113.2644), // Guangzhou
            Pair(30.5728, 104.0668), // Chengdu
            Pair(22.5431, 114.0579), // Shenzhen
            Pair(34.3416, 108.9398), // Xi'an
            Pair(45.8038, 126.5350)  // Harbin
        )

        for ((lat, lng) in testLocations) {
            val (gcjLat, gcjLng) = CoordinateTransform.wgs84ToGcj02(lat, lng)
            val (wgsLat, wgsLng) = CoordinateTransform.gcj02ToWgs84(gcjLat, gcjLng)

            assertEquals("City lat restore ($lat, $lng)", lat, wgsLat, 0.000001)
            assertEquals("City lng restore ($lat, $lng)", lng, wgsLng, 0.000001)
        }
    }
}
