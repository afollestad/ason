package com.afollestad.json;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AsonPrimitivesTest {

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

//    @Test public void char_test() {
//        AsonArray<Character> result = AsonArray.serialize(new char[]{'a', 'b', 'c', 'd'});
//        String output = result.toString();
//        assertEquals("[a,b,c,d]", output);
//
//        char[] parsed = AsonArray.deserializeArray(new AsonArray<Character>(output));
//        assertEquals(1, parsed[0]);
//        assertEquals(2, parsed[1]);
//        assertEquals(3, parsed[2]);
//        assertEquals(4, parsed[3]);
//    }
}