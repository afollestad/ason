package com.afollestad.ason;

import org.junit.Test;

import static org.junit.Assert.*;

public class AsonPrimitivesTest {

  //
  ////// SERIALIZATION
  //

  @Test
  public void short_test() {
    AsonArray<Short> result = Ason.serializeArray(new short[] {1, 2, 3, 4});
    String output = result.toString();
    assertEquals("[1,2,3,4]", output);

    short[] parsed = Ason.deserialize(output, short[].class);
    assertEquals(1, parsed[0]);
    assertEquals(2, parsed[1]);
    assertEquals(3, parsed[2]);
    assertEquals(4, parsed[3]);
  }

  @Test
  public void int_test() {
    AsonArray<Integer> result = Ason.serializeArray(new int[] {1, 2, 3, 4});
    String output = result.toString();
    assertEquals("[1,2,3,4]", output);

    int[] parsed = Ason.deserialize(output, int[].class);
    assertEquals(1, parsed[0]);
    assertEquals(2, parsed[1]);
    assertEquals(3, parsed[2]);
    assertEquals(4, parsed[3]);
  }

  @Test
  public void long_test() {
    AsonArray<Long> result = Ason.serializeArray(new long[] {1L, 2L, 3L, 4L});
    String output = result.toString();
    assertEquals("[1,2,3,4]", output);

    long[] parsed = Ason.deserialize(output, long[].class);
    assertEquals(1L, parsed[0]);
    assertEquals(2L, parsed[1]);
    assertEquals(3L, parsed[2]);
    assertEquals(4L, parsed[3]);
  }

  @Test
  public void float_test() {
    AsonArray<Float> result = Ason.serializeArray(new float[] {1f, 2f, 3f, 4f});
    String output = result.toString();
    assertEquals("[1,2,3,4]", output);

    float[] parsed = Ason.deserialize(output, float[].class);
    assertEquals(1f, parsed[0], 1f);
    assertEquals(2f, parsed[1], 1f);
    assertEquals(3f, parsed[2], 1f);
    assertEquals(4f, parsed[3], 1f);
  }

  @Test
  public void double_test() {
    AsonArray<Double> result = Ason.serializeArray(new double[] {1d, 2d, 3d, 4d});
    String output = result.toString();
    assertEquals("[1,2,3,4]", output);

    double[] parsed = Ason.deserialize(output, double[].class);
    assertEquals(1d, parsed[0], 1d);
    assertEquals(2d, parsed[1], 1d);
    assertEquals(3d, parsed[2], 1d);
    assertEquals(4d, parsed[3], 1d);
  }

  @Test
  public void char_test() {
    AsonArray<Character> result = Ason.serializeArray(new char[] {'a', 'b', 'c', 'd'});
    String output = result.toString();
    assertEquals("[\"a\",\"b\",\"c\",\"d\"]", output);

    char[] parsed = Ason.deserialize(output, char[].class);
    assertEquals('a', parsed[0]);
    assertEquals('b', parsed[1]);
    assertEquals('c', parsed[2]);
    assertEquals('d', parsed[3]);
  }

  @Test
  public void byte_test() {
    AsonArray<Byte> result = Ason.serializeArray(new byte[] {1, 2, 3, 4});
    String output = result.toString();
    assertEquals("[1,2,3,4]", output);

    int[] parsed = Ason.deserialize(output, int[].class);
    assertEquals(1, parsed[0]);
    assertEquals(2, parsed[1]);
    assertEquals(3, parsed[2]);
    assertEquals(4, parsed[3]);
  }

  @Test
  public void boolean_test() {
    AsonArray<Boolean> result = Ason.serializeArray(new boolean[] {true, false, true, false});
    String output = result.toString();
    assertEquals("[true,false,true,false]", output);

    boolean[] parsed = Ason.deserialize(output, boolean[].class);
    assertEquals(true, parsed[0]);
    assertEquals(false, parsed[1]);
    assertEquals(true, parsed[2]);
    assertEquals(false, parsed[3]);
  }

  //
  ////// GET/PUT
  //

  @Test
  public void put_deep_test() {
    Ason inside = new Ason().put("id", 2);
    Ason ason = new Ason().put("id", 1).put("inside", inside).put("inside.test", "Hello, world!");
    assertEquals(1, ason.get("id"));
    assertEquals(2, ason.get("inside.id"));
    assertEquals("Hello, world!", ason.get("inside.test"));
  }

  @Test
  public void put_primitive_array_test() {
    Ason ason = new Ason().put("test", 1, 2, 3, 4);
    AsonArray<Integer> array = ason.get("test");
    assertNotNull(array);
    assertEquals(4, array.size());
    assertEquals(1, array.get(0).intValue());
    assertEquals(2, array.get(1).intValue());
    assertEquals(3, array.get(2).intValue());
    assertEquals(4, array.get(3).intValue());
  }

  @Test
  public void get_short_test() {
    Ason ason = new Ason().put("test", (short) 22);
    assertEquals((short) 22, ason.get("test"));
    assertEquals((short) 22, ason.getShort("test"));
    assertEquals((short) 69, ason.getShort("test2", (short) 69));
  }

  @Test
  public void get_int_test() {
    Ason ason = new Ason().put("test", 1995);
    assertEquals(1995, ason.get("test"));
    assertEquals(1995, ason.getInt("test"));
    assertEquals(1996, ason.getInt("test2", 1996));
  }

  @Test
  public void get_long_test() {
    Ason ason = new Ason().put("test", 1995L);
    assertEquals(1995L, ason.get("test"));
    assertEquals(1995L, ason.getLong("test"));
    assertEquals(1996L, ason.getLong("test2", 1996L));
  }

  @Test
  public void get_float_test() {
    Ason ason = new Ason().put("test", 1995.5f);
    assertEquals(1995.5f, ason.get("test"));
    assertEquals(1995.5f, ason.getFloat("test"), 0f);
    assertEquals(1996f, ason.getFloat("test2", 1996f), 0f);
  }

  @Test
  public void get_double_test() {
    Ason ason = new Ason().put("test", 1995d);
    assertEquals(1995d, ason.get("test"));
    assertEquals(1995d, ason.getDouble("test"), 0d);
    assertEquals(1996d, ason.getDouble("test2", 1996d), 0d);
  }

  @Test
  public void get_char_test() {
    Ason ason = new Ason().put("test", 'a');
    assertEquals('a', ason.getChar("test").charValue());
    assertEquals('b', ason.getChar("test2", 'b').charValue());
  }

  @Test
  public void get_byte_test() {
    Ason ason = new Ason().put("test", (byte) 255);
    assertEquals((byte) 255, ason.getByte("test"));
    assertEquals((byte) 124, ason.getByte("test2", (byte) 124));
  }

  @Test
  public void get_boolean_test() {
    Ason ason = new Ason().put("test1", true).put("test2", false);
    assertEquals(true, ason.get("test1"));
    assertTrue(ason.getBool("test1"));
    assertFalse(ason.getBool("test2"));
    assertTrue(ason.getBool("test3", true));
  }
}
