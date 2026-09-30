package app.paybak.paybak.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PhotoDecodingTest {
    @Test
    fun decodesAtTheLargestPowerOfTwoThatStillCoversTheTarget() {
        assertEquals(1, sampleSize(shortSide = 512, target = 512))
        assertEquals(1, sampleSize(shortSide = 1023, target = 512))
        assertEquals(2, sampleSize(shortSide = 1024, target = 512))
        assertEquals(4, sampleSize(shortSide = 3024, target = 512))
    }

    @Test
    fun smallImagesAreNotSampled() {
        assertEquals(1, sampleSize(shortSide = 200, target = 512))
    }
}
