package com.snizhy.volumecontrol
import org.junit.Assert.assertEquals
import org.junit.Test
class VolumeMathTest{
 @Test fun boundaries(){assertEquals(0,(0).coerceIn(0,100));assertEquals(100,(100).coerceIn(0,100));assertEquals(100,120.coerceIn(0,100));assertEquals(0,(-2).coerceIn(0,100))}
 @Test fun conversion(){val max=15;assertEquals(9,((60f/100f)*max).toInt());assertEquals(60,((9f/max)*100).toInt())}
}