package com.afollestad.ason;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class AsonArrayTest {

  private AsonArray<Ason> array;

  @Before
  public void setup() {
    array =
        new AsonArray<Ason>()
            .add(new Ason().put("_id", 1).put("name", "Aidan").put("attrs.priority", 2))
            .add(new Ason().put("_id", 2).put("name", "Waverly").put("attrs.priority", 1));
  }

  @Test
  public void invalid_json_test() {
    try {
      new AsonArray<>("Hello, world!");
      assertFalse("No exception thrown for invalid JSON!", false);
    } catch (InvalidJsonException ignored) {
    }
  }

  @Test
  public void empty_json_test() {
    AsonArray<Integer> array = new AsonArray<>((String) null);
    assertTrue(array.isEmpty());
  }

  @Test
  public void out_of_bounds_test() {
    AsonArray<Integer> array = new AsonArray<Integer>().add(1, 2, 3, 4);
    try {
      array.get(9);
      assertFalse("No exception was thrown for an out of bounds index!", false);
    } catch (IndexOutOfBoundsException ignored) {
    }
    try {
      array.getJsonArray(9);
      assertFalse("No exception was thrown for an out of bounds index!", false);
    } catch (IndexOutOfBoundsException ignored) {
    }
    try {
      array.getJsonObject(9);
      assertFalse("No exception was thrown for an out of bounds index!", false);
    } catch (IndexOutOfBoundsException ignored) {
    }
    try {
      array.equal(9, 21);
      assertFalse("No exception was thrown for an out of bounds index!", false);
    } catch (IndexOutOfBoundsException ignored) {
    }
    try {
      array.remove(20);
      assertFalse("No exception was thrown for an out of bounds index!", false);
    } catch (IndexOutOfBoundsException ignored) {
    }
    try {
      array.equal(21, "name", "Aidan");
      assertFalse("No exception was thrown for an out of bounds index!", false);
    } catch (IndexOutOfBoundsException ignored) {
    }
    try {
      array.get(21, "name", Integer.class);
      assertFalse("No exception was thrown for an out of bounds index!", false);
    } catch (IndexOutOfBoundsException ignored) {
    }
  }

  @Test
  public void get_object_test() {
    Ason ason = array.getJsonObject(0);
    assertNotNull(ason);
    assertEquals(1, ason.get("_id"));
  }

  @Test
  public void get_array_test() {
    AsonArray<Integer> arrayItem = new AsonArray<Integer>().add(1, 2, 3, 4);
    AsonArray<AsonArray> array = new AsonArray<AsonArray>().add(arrayItem);

    AsonArray getArray = array.getJsonArray(0);
    assertNotNull(getArray);
    assertEquals(1, getArray.get(0));
    assertEquals(2, getArray.get(1));
    assertEquals(3, getArray.get(2));
    assertEquals(4, getArray.get(3));
  }

  @Test
  public void get_object_array_null_test() {
    AsonArray<Integer> idk = new AsonArray<Integer>().add(1, 2, 3, 4);
    Ason object = idk.getJsonObject(0);
    assertNull(object);
    AsonArray array = idk.getJsonArray(0);
    assertNull(array);
  }

  @Test
  public void put_null_test() {
    AsonArray<Integer> array = new AsonArray<Integer>().addNull().add((Integer[]) null);
    assertTrue(array.equal(0, null));
    assertTrue(array.equal(1, null));
  }

  @Test
  public void builder_test() {
    String expected =
        "[{\"name\":\"Aidan\",\"_id\":1,\"attrs\":{\"priority\":2}},"
            + "{\"name\":\"Waverly\",\"_id\":2,\"attrs\":{\"priority\":1}}]";
    assertEquals(array.toString(), expected);
  }

  @Test
  public void from_string_test() {
    assertEquals(array.size(), 2);

    assertTrue(array.equal(0, "name", "Aidan"));
    assertTrue(array.equal(0, "_id", 1));
    assertTrue(array.equal(0, "attrs.priority", 2));

    assertTrue(array.equal(1, "name", "Waverly"));
    assertTrue(array.equal(1, "_id", 2));
    assertTrue(array.equal(1, "attrs.priority", 1));
  }

  @Test
  public void remove_test() {
    Ason one = new Ason().put("_id", 1).put("name", "Aidan").put("attrs.priority", 2);
    Ason two = new Ason().put("_id", 2).put("name", "Waverly").put("attrs.priority", 1);
    array = new AsonArray<Ason>().add(one).add(two);
    array.remove(0);
    assertEquals(two, array.get(0));
    assertTrue(array.equal(0, two));
  }

  @Test
  public void test_pretty_print() {
    array = new AsonArray<Ason>().add(new Ason().put("_id", 1)).add(new Ason().put("_id", 2));
    assertEquals("[\n" + "    {\"_id\": 1},\n" + "    {\"_id\": 2}\n" + "]", array.toString(4));
  }

  @Test
  public void test_string_array() {
    AsonArray<String> array = new AsonArray<String>().add("Hello", "World!");
    assertEquals("[\"Hello\",\"World!\"]", array.toString());
  }

  @Test
  public void test_primitive_array() {
    AsonArray<Integer> array = new AsonArray<Integer>().add(1, 2, 3, 4);
    assertEquals("[1,2,3,4]", array.toString());
  }

  @Test
  public void test_array_in_array() {
    AsonArray<Integer> one = new AsonArray<Integer>().add(1, 2, 3, 4);
    AsonArray<Integer> two = new AsonArray<Integer>().add(5, 6, 7, 8);
    AsonArray<AsonArray> parent = new AsonArray<AsonArray>().add(one, two);

    assertEquals(2, parent.size());
    //noinspection unchecked
    AsonArray<Integer> pullTwo = parent.get(1);
    assertNotNull(pullTwo);
    assertEquals(5, pullTwo.get(0).intValue());
    assertEquals(6, pullTwo.get(1).intValue());
    assertEquals(7, pullTwo.get(2).intValue());
    assertEquals(8, pullTwo.get(3).intValue());
  }

  @Test
  public void test_to_list() {
    AsonArray<Integer> array = new AsonArray<Integer>().add(1, 2, 3, 4);
    List<Integer> list = array.toList();
    assertEquals(4, list.size());
    assertEquals(1, list.get(0).intValue());
    assertEquals(2, list.get(1).intValue());
    assertEquals(3, list.get(2).intValue());
    assertEquals(4, list.get(3).intValue());
  }

  @Test
  public void test_iterator() {
    AsonArray<Integer> array = new AsonArray<Integer>().add(1, 2, 3, 4);
    int index = 0;
    for (Integer ignored : array) {
      index++;
    }
    assertEquals(4, index);
  }

  @Test
  public void deep_equal_test() {
    Ason one = new Ason().put("name", "Aidan").put("born", 1995);
    Ason two = new Ason().put("name", "Waverly").put("born", 1997);
    AsonArray<Ason> array = new AsonArray<Ason>().add(one, two).addNull();
    assertTrue(array.equal(0, "name", "Aidan"));
    assertTrue(array.equal(1, "name", "Waverly"));
    assertTrue(array.equal(0, "idk", null));
    assertTrue(array.equal(2, "idk", null));
  }

  @Test
  public void primitive_equal_deep_error_test() {
    AsonArray<Integer> idk = new AsonArray<Integer>().add(1, 2, 3, 4);
    try {
      idk.equal(0, "name", "Aidan");
      assertFalse(
          "An exception was not thrown when using path equality on a primitive array!", false);
    } catch (InvalidPathException ignored) {
    }
  }
}
