package com.afollestad.ason;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AsonPrimitivesTest {

    @Test public void short_test() {
        AsonArray<Short> result = Ason.serializeArray(new short[]{1, 2, 3, 4});
        String output = result.toString();
        assertEquals("[1,2,3,4]", output);

        short[] parsed = Ason.deserialize(output, short[].class);
        assertEquals(1, parsed[0]);
        assertEquals(2, parsed[1]);
        assertEquals(3, parsed[2]);
        assertEquals(4, parsed[3]);
    }

    @Test public void int_test() {
        AsonArray<Integer> result = Ason.serializeArray(new int[]{1, 2, 3, 4});
        String output = result.toString();
        assertEquals("[1,2,3,4]", output);

        int[] parsed = Ason.deserialize(output, int[].class);
        assertEquals(1, parsed[0]);
        assertEquals(2, parsed[1]);
        assertEquals(3, parsed[2]);
        assertEquals(4, parsed[3]);
    }

    @Test public void long_test() {
        AsonArray<Long> result = Ason.serializeArray(new long[]{1L, 2L, 3L, 4L});
        String output = result.toString();
        assertEquals("[1,2,3,4]", output);

        long[] parsed = Ason.deserialize(output, long[].class);
        assertEquals(1L, parsed[0]);
        assertEquals(2L, parsed[1]);
        assertEquals(3L, parsed[2]);
        assertEquals(4L, parsed[3]);
    }

    @Test public void float_test() {
        AsonArray<Float> result = Ason.serializeArray(new float[]{1f, 2f, 3f, 4f});
        String output = result.toString();
        assertEquals("[1,2,3,4]", output);

        float[] parsed = Ason.deserialize(output, float[].class);
        assertEquals(1f, parsed[0], 1f);
        assertEquals(2f, parsed[1], 1f);
        assertEquals(3f, parsed[2], 1f);
        assertEquals(4f, parsed[3], 1f);
    }

    @Test public void double_test() {
        AsonArray<Double> result = Ason.serializeArray(new double[]{1d, 2d, 3d, 4d});
        String output = result.toString();
        assertEquals("[1,2,3,4]", output);

        double[] parsed = Ason.deserialize(output, double[].class);
        assertEquals(1d, parsed[0], 1d);
        assertEquals(2d, parsed[1], 1d);
        assertEquals(3d, parsed[2], 1d);
        assertEquals(4d, parsed[3], 1d);
    }

    @Test public void char_test() {
        AsonArray<Character> result = Ason.serializeArray(new char[]{'a', 'b', 'c', 'd'});
        String output = result.toString();
        assertEquals("[\"a\",\"b\",\"c\",\"d\"]", output);

        char[] parsed = Ason.deserialize(output, char[].class);
        assertEquals('a', parsed[0]);
        assertEquals('b', parsed[1]);
        assertEquals('c', parsed[2]);
        assertEquals('d', parsed[3]);
    }

    @Test public void byte_test() {
        AsonArray<Byte> result = Ason.serializeArray(new byte[]{1, 2, 3, 4});
        String output = result.toString();
        assertEquals("[1,2,3,4]", output);

        int[] parsed = Ason.deserialize(output, int[].class);
        assertEquals(1, parsed[0]);
        assertEquals(2, parsed[1]);
        assertEquals(3, parsed[2]);
        assertEquals(4, parsed[3]);
    }

    @Test public void boolean_test() {
        AsonArray<Boolean> result = Ason.serializeArray(new boolean[]{true, false, true, false});
        String output = result.toString();
        assertEquals("[true,false,true,false]", output);

        boolean[] parsed = Ason.deserialize(output, boolean[].class);
        assertEquals(true, parsed[0]);
        assertEquals(false, parsed[1]);
        assertEquals(true, parsed[2]);
        assertEquals(false, parsed[3]);
    }
}