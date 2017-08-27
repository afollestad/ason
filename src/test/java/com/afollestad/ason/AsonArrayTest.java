package com.afollestad.ason;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class AsonArrayTest {

  private AsonArray<Ason> array;

  @Before
  public void setup() {
    array =
        new AsonArray<Ason>()
            .add(new Ason().put("_id", 1).put("name", "Aidan").put("attrs.priority", 2))
            .add(new Ason().put("_id", 2).put("name", "Nina").put("attrs.priority", 1));
  }

  @Test(expected = InvalidJsonException.class)
  public void invalid_json_test() {
    new AsonArray<>("Hello, world!");
  }

  @Test
  public void empty_json_test() {
    AsonArray<Integer> array = new AsonArray<>((String) null);
    assertTrue(array.isEmpty());
  }

  @Test
  public void out_of_bounds_test() {
    AsonArray<Integer> array = new AsonArray<Integer>().add(1, 2, 3, 4);
    assertNull(array.get(9));
    try {
      array.getJsonArray(9);
      fail("No exception was thrown for an out of bounds index!");
    } catch (IndexOutOfBoundsException ignored) {
    }
    try {
      array.getJsonObject(9);
      fail("No exception was thrown for an out of bounds index!");
    } catch (IndexOutOfBoundsException ignored) {
    }
    assertTrue(array.equal(9, null));
    try {
      array.remove(20);
      fail("No exception was thrown for an out of bounds index!");
    } catch (IndexOutOfBoundsException ignored) {
    }
    try {
      array.equal(21, "name", "Aidan");
      fail("No exception was thrown for an out of bounds index!");
    } catch (IndexOutOfBoundsException ignored) {
    }
    try {
      array.get(21, "name", Integer.class);
      fail("No exception was thrown for an out of bounds index!");
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
            + "{\"name\":\"Nina\",\"_id\":2,\"attrs\":{\"priority\":1}}]";
    assertEquals(array.toString(), expected);
  }

  @Test
  public void from_string_test() {
    assertEquals(array.size(), 2);

    assertTrue(array.equal(0, "name", "Aidan"));
    assertTrue(array.equal(0, "_id", 1));
    assertTrue(array.equal(0, "attrs.priority", 2));

    assertTrue(array.equal(1, "name", "Nina"));
    assertTrue(array.equal(1, "_id", 2));
    assertTrue(array.equal(1, "attrs.priority", 1));
  }

  @Test
  public void remove_test() {
    Ason one = new Ason().put("_id", 1).put("name", "Aidan").put("attrs.priority", 2);
    Ason two = new Ason().put("_id", 2).put("name", "Nina").put("attrs.priority", 1);
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
  public void test_array_in_array_deserialize() {
    AsonArray<Integer[]> parent =
        new AsonArray<Integer[]>().add(new Integer[] {1, 2, 3, 4}, new Integer[] {5, 6, 7, 8});
    assertEquals(2, parent.size());

    Integer[] arrayOne = parent.get(0, Integer[].class);
    assertNotNull(arrayOne);
    assertEquals(arrayOne.length, 4);
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
    Ason two = new Ason().put("name", "Nina").put("born", 1997);
    AsonArray<Ason> array = new AsonArray<Ason>().add(one, two).addNull();
    assertTrue(array.equal(0, "name", "Aidan"));
    assertTrue(array.equal(1, "name", "Nina"));
    assertTrue(array.equal(0, "idk", null));
    assertTrue(array.equal(2, "idk", null));
  }

  @Test(expected = InvalidPathException.class)
  public void primitive_equal_deep_error_test() {
    AsonArray<Integer> idk = new AsonArray<Integer>().add(1, 2, 3, 4);
    idk.equal(0, "name", "Aidan");
  }

  @SuppressWarnings("unchecked")
  @Test(expected = IllegalStateException.class)
  public void test_get_with_path_on_primitive() {
    AsonArray array = new AsonArray().add(1, 2, 3, 4);
    array.get(2, "idk.name", SimpleTestDataOne.class);
  }

  @Test
  public void test_get_with_path_nulls() {
    AsonArray<SimpleTestDataOne> array = new AsonArray<SimpleTestDataOne>().addNull().addNull();
    assertNull(array.get(1, "idk.name", SimpleTestDataOne.class));
  }

  @Test
  public void test_to_list_null_values() {
    AsonArray array = new AsonArray().addNull().addNull();
    List list = array.toList();
    assertNull(list.get(0));
    assertNull(list.get(1));
  }

  @Test
  public void test_auto_deserialize_null() {
    AsonArray<SimpleTestDataOne> array = new AsonArray<SimpleTestDataOne>().addNull().addNull();
    SimpleTestDataOne data = array.get(0, SimpleTestDataOne.class);
    assertNull(data);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test_get_list() {
    List<Integer> one = new ArrayList<>(4);
    one.add(1);
    one.add(2);
    one.add(3);
    one.add(4);

    List<Integer> two = new ArrayList<>(4);
    two.add(5);
    two.add(6);
    two.add(7);
    two.add(8);

    AsonArray<List<Integer>> array = new AsonArray<List<Integer>>().add(one, two);

    one = array.getList(0, Integer.class);
    assertNotNull(one);
    assertEquals(1, one.get(0).intValue());
    assertEquals(2, one.get(1).intValue());
    assertEquals(3, one.get(2).intValue());
    assertEquals(4, one.get(3).intValue());

    two = array.getList(1, Integer.class);
    assertNotNull(two);
    assertEquals(5, two.get(0).intValue());
    assertEquals(6, two.get(1).intValue());
    assertEquals(7, two.get(2).intValue());
    assertEquals(8, two.get(3).intValue());
  }

  @Test
  public void test_get_list_null_items() {
    AsonArray<List<Integer>> array = new AsonArray<List<Integer>>().addNull().addNull();
    assertNull(array.getList(0, Integer.class));
    assertNull(array.getList(1, Integer.class));
  }

  @Test(expected = IllegalStateException.class)
  public void test_get_list_non_array_items() {
    AsonArray<Integer> array = new AsonArray<Integer>().add(1, 2, 3, 4);
    array.getList(0, Integer.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_get_list_wrong_cls() {
    AsonArray<Integer> array = new AsonArray<Integer>().add(1, 2, 3, 4);
    array.getList(0, List.class);
  }

  @Test(expected = IllegalStateException.class)
  public void test_regular_get_list() {
    AsonArray<List> array = new AsonArray<List>().addNull().addNull();
    array.get(0, List.class);
  }
}
