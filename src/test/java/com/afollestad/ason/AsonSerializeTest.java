package com.afollestad.ason;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

/** @author Aidan Follestad (afollestad) */
public class AsonSerializeTest {

  //
  ////// SERIALIZE
  //

  @Test
  public void test_serialize() {
    Person person = new Person(2, "Aidan", 22);
    person.spouse = new Person(6, "Nina", 22);
    Ason ason = Ason.serialize(person);
    assertEquals("Aidan", ason.get("name"));
    assertEquals(2, ason.get("_id"));
    assertEquals(22, ason.get("age"));
    Ason spouse = ason.get("spouse");
    assertEquals("Nina", spouse.get("name"));
    assertEquals(6, spouse.get("_id"));
    assertEquals(22, spouse.get("age"));
  }

  @Test
  public void test_serialize_array() {
    Person[] people = new Person[] {new Person(1, "Aidan", 22), new Person(2, "Nina", 22)};
    AsonArray<Person> json = Ason.serializeArray(people);

    Ason one = json.getJsonObject(0);
    assertEquals("Aidan", one.get("name"));
    assertEquals(1, one.get("_id"));
    assertEquals(22, one.get("age"));

    Ason two = json.getJsonObject(1);
    assertEquals("Nina", two.get("name"));
    assertEquals(2, two.get("_id"));
    assertEquals(22, two.get("age"));
  }

  @Test
  public void test_serialize_list() {
    List<Person> people = new ArrayList<>(2);
    people.add(new Person(1, "Aidan", 22));
    people.add(new Person(2, "Nina", 22));
    AsonArray<Person> json = Ason.serializeList(people);

    Ason one = json.getJsonObject(0);
    assertEquals("Aidan", one.get("name"));
    assertEquals(1, one.get("_id"));
    assertEquals(22, one.get("age"));

    Ason two = json.getJsonObject(1);
    assertEquals("Nina", two.get("name"));
    assertEquals(2, two.get("_id"));
    assertEquals(22, two.get("age"));
  }

  @Test
  public void test_put_object_serialize() {
    Ason object = new Ason();
    Person person = new Person(1, "Aidan", 22);
    object.put("person", person);

    assertEquals("Aidan", object.get("person.name"));
    assertEquals(1, object.get("person._id"));
    assertEquals(22, object.get("person.age"));
  }

  @Test
  public void test_put_array_serialize() {
    AsonArray<Person> array = new AsonArray<>();
    Person person = new Person(1, "Aidan", 22);
    array.add(person);

    Ason first = array.getJsonObject(0);
    assertEquals("Aidan", first.get("name"));
    assertEquals(1, first.get("_id"));
    assertEquals(22, first.get("age"));
  }

  @Test
  public void test_primitive_serialize() {
    int[] ids = new int[] {1, 2, 3, 4};
    AsonArray<Integer> array = Ason.serializeArray(ids);
    assertEquals("[1,2,3,4]", array.toString());
  }

  @Test
  public void test_serialize_with_array() {
    Person2 person = new Person2(1);
    person.family = new Person2[] {new Person2(2), new Person2(3), new Person2(4)};

    Ason ason = Ason.serialize(person);
    AsonArray<Person3> array = ason.get("family");
    assertNotNull(array);
    assertEquals(array.size(), 3);
  }

  @Test
  public void test_serialize_with_list() {
    Person3 person = new Person3(1);
    person.family.add(new Person3(2));
    person.family.add(new Person3(3));
    person.family.add(new Person3(4));

    Ason ason = Ason.serialize(person);
    AsonArray<Person3> array = ason.get("family");
    assertNotNull(array);
    assertEquals(array.size(), 3);
  }

  @Test
  public void test_recursive_serialize() {
    Superhero person = new Superhero(2, "Aidan", 22, "Flight");
    person.spouse = new Person(6, "Nina", 22);
    Ason ason = Ason.serialize(person, true);
    assertEquals("Aidan", ason.get("name"));
    assertEquals(2, ason.get("_id"));
    assertEquals(22, ason.get("age"));
    assertEquals("Flight", ason.get("power"));
  }

  @Test
  public void test_recursive_serialize_only_super_fields() {
    EmptyPerson person = new EmptyPerson(2, "Aidan", 22);
    person.spouse = new Person(6, "Nina", 22);
    Ason ason = Ason.serialize(person, true);
    assertEquals("Aidan", ason.get("name"));
    assertEquals(2, ason.get("_id"));
    assertEquals(22, ason.get("age"));
  }

  //
  ////// DESERIALIZE
  //

  @Test
  public void test_deserialize() {
    String input =
        "{\"name\":\"Aidan\",\"_id\":2,\"age\":22,"
            + "\"spouse\":{\"name\":\"Nina\",\"_id\":6,\"age\":22}}";
    Ason ason = new Ason(input);
    Person person = ason.deserialize(Person.class);
    assertEquals(person.name, "Aidan");
    assertEquals(person.id, 2);
    assertEquals(person.age, 22);

    assertEquals(person.spouse.name, "Nina");
    assertEquals(person.spouse.id, 6);
    assertEquals(person.spouse.age, 22);
  }

  @Test
  public void test_deserialize_array() {
    String input =
        "[{\"name\":\"Aidan\",\"_id\":1,\"age\":22}," + "{\"name\":\"Nina\",\"_id\":2,\"age\":22}]";
    AsonArray<Person> array = new AsonArray<>(input);
    Person[] people = array.deserialize(Person[].class);

    assertEquals(people[0].name, "Aidan");
    assertEquals(people[0].id, 1);
    assertEquals(people[0].age, 22);

    assertEquals(people[1].name, "Nina");
    assertEquals(people[1].id, 2);
    assertEquals(people[1].age, 22);
  }

  @Test
  public void test_deserialize_list() {
    String input =
        "[{\"name\":\"Aidan\",\"_id\":1,\"age\":22}," + "{\"name\":\"Nina\",\"_id\":2,\"age\":22}]";
    AsonArray<Person> array = new AsonArray<>(input);
    List<Person> people = array.deserializeList(Person.class);

    assertEquals(people.get(0).name, "Aidan");
    assertEquals(people.get(0).id, 1);
    assertEquals(people.get(0).age, 22);

    assertEquals(people.get(1).name, "Nina");
    assertEquals(people.get(1).id, 2);
    assertEquals(people.get(1).age, 22);
  }

  @Test
  public void test_deserialize_string_object() {
    String input =
        "{\"name\":\"Aidan\",\"_id\":2,\"age\":22,"
            + "\"spouse\":{\"name\":\"Nina\",\"_id\":6,\"age\":22}}";
    Person object = Ason.deserialize(input, Person.class);
    assertNotNull(object);
  }

  @Test
  public void test_deserialize_string_array() {
    String input =
        "[{\"name\":\"Aidan\",\"_id\":1,\"age\":22}," + "{\"name\":\"Nina\",\"_id\":2,\"age\":22}]";
    Person[] object = Ason.deserialize(input, Person[].class);
    assertEquals(object.length, 2);
  }

  @Test
  public void test_deserialize_string_list() {
    String input =
        "[{\"name\":\"Aidan\",\"_id\":1,\"age\":22}," + "{\"name\":\"Nina\",\"_id\":2,\"age\":22}]";
    List<Person> object = Ason.deserializeList(input, Person.class);
    assertEquals(object.size(), 2);
  }

  @Test
  public void test_get_object_deserialize() {
    String input = "{\"person\":{\"name\":\"Aidan\",\"_id\":1,\"age\":22}}";
    Ason ason = new Ason(input);
    Person person = ason.get("person", Person.class);
    assertEquals(person.name, "Aidan");
    assertEquals(person.id, 1);
    assertEquals(person.age, 22);
  }

  @Test
  public void test_get_array_deserialize() {
    String input = "[{\"name\":\"Aidan\",\"_id\":1,\"age\":22}]";
    AsonArray<Person> json = new AsonArray<>(input);
    Person person = json.get(0, Person.class);
    assertEquals(person.name, "Aidan");
    assertEquals(person.id, 1);
    assertEquals(person.age, 22);
  }

  @Test
  public void test_primitive_deserialize() {
    AsonArray<Integer> array = new AsonArray<Integer>().add(1, 2, 3, 4);
    int[] primitive = Ason.deserialize(array, int[].class);
    assertEquals(1, primitive[0]);
    assertEquals(2, primitive[1]);
    assertEquals(3, primitive[2]);
    assertEquals(4, primitive[3]);
  }

  @Test
  public void test_deserialize_with_array() {
    String input = "{\"_id\":1,\"family\":[{\"_id\":2},{\"_id\":3},{\"_id\":4}]}";
    Person2 result = Ason.deserialize(input, Person2.class);
    assertEquals(result.family.length, 3);
    assertEquals(2, result.family[0].id);
    assertEquals(3, result.family[1].id);
    assertEquals(4, result.family[2].id);
  }

  @Test
  public void test_deserialize_with_list() {
    String input = "{\"_id\":1,\"family\":[{\"_id\":2},{\"_id\":3},{\"_id\":4}]}";
    Person3 result = Ason.deserialize(input, Person3.class);
    assertEquals(result.family.size(), 3);
    assertEquals(2, result.family.get(0).id);
    assertEquals(3, result.family.get(1).id);
    assertEquals(4, result.family.get(2).id);
  }

  @Test
  public void test_serialize_deserialize_null() {
    assertNull(Ason.serialize(null));
    assertNull(Ason.deserialize((Ason) null, Issue10Example.class));
    assertNull(Ason.serializeArray(null));
    assertNull(Ason.serializeList(null));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_serialize_primitive() {
    Ason.serialize(1);
  }

  @Test
  public void test_serialize_ason_object_array() {
    try {
      Ason.serialize(new Ason());
      assertFalse("No exception thrown when serializing Ason class!", false);
    } catch (IllegalArgumentException ignored) {
    }
    try {
      Ason.serialize(new AsonArray());
      assertFalse("No exception thrown when serializing Ason class!", false);
    } catch (IllegalArgumentException ignored) {
    }
    try {
      Ason.serialize(new JSONObject());
      assertFalse("No exception thrown when serializing org.json class!", false);
    } catch (IllegalArgumentException ignored) {
    }
    try {
      Ason.serialize(new JSONArray());
      assertFalse("No exception thrown when serializing org.json class!", false);
    } catch (IllegalArgumentException ignored) {
    }
  }

  @Test
  public void test_serialize_array_list_wrong_method() {
    try {
      Ason.serialize(new int[] {1, 2, 3, 4});
      assertFalse("No exception thrown when using serialize() on array!", false);
    } catch (IllegalArgumentException ignored) {
    }
    try {
      List<Integer> list = new ArrayList<>(0);
      Ason.serialize(list);
      assertFalse("No exception thrown when using serialize() on List!", false);
    } catch (IllegalArgumentException ignored) {
    }
    try {
      Ason.serializeArray("Hello");
      assertFalse("No exception thrown when using serializeArray() on non-array!", false);
    } catch (IllegalArgumentException ignored) {
    }
  }

  @Test
  public void test_serialize_empty_array() {
    AsonArray array = Ason.serializeArray(new int[] {});
    assertTrue(array.isEmpty());
  }

  @Test(expected = RuntimeException.class)
  public void test_serialize_inaccessible_field() throws Exception {
    Field hi = SimpleTestDataOne.class.getDeclaredField("hi");
    AsonSerializer.get().serializeField(hi, new SimpleTestDataTwo());
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_deserialize_primitive_cls() {
    Ason.deserialize("{\"hi\":\"hello\"}", Integer.class);
  }

  @Test
  public void test_deserialize_ason_json() {
    Ason ason = new Ason("{\"hello\":\"hi\"}");
    Ason deserializeAson = Ason.deserialize(ason, Ason.class);
    JSONObject deserializeJson = Ason.deserialize(ason, JSONObject.class);
    assertEquals(deserializeAson, ason);
    assertEquals("hi", deserializeJson.optString("hello"));
  }

  @Test
  public void test_deserialize_object_to_array() {
    Ason ason = new Ason();
    try {
      Ason.deserialize(ason, AsonArray.class);
      assertFalse("No exception thrown when deserializing object to array!", false);
    } catch (IllegalArgumentException ignored) {
    }
    try {
      Ason.deserialize(ason, JSONArray.class);
      assertFalse("No exception thrown when deserializing object to array!", false);
    } catch (IllegalArgumentException ignored) {
    }
  }

  @Test
  public void test_deserialize_empty_array() {
    int[] test = Ason.deserialize(new AsonArray(), int[].class);
    assertEquals(0, test.length);
  }

  @Test
  public void test_deserialize_empty_list() {
    List<Integer> test = Ason.deserializeList(new AsonArray(), Integer.class);
    assertTrue(test.isEmpty());
  }

  @Test
  public void test_deserialize_null_json() {
    SimpleTestDataOne one = AsonSerializer.get().deserialize(null, SimpleTestDataOne.class);
    assertNull(one);
  }

  @Test(expected = IllegalStateException.class)
  public void test_deserialize_wrong_object_target() {
    Ason ason = new Ason("{\"obj\":{\"hi\":\"hello\"}}");
    ason.get("obj.hi", SimpleTestDataOne.class);
  }

  @Test(expected = IllegalStateException.class)
  public void test_deserialize_wrong_array_target() {
    Ason ason = new Ason("{\"obj\":[\"hi\",\"hello\"]}");
    ason.get("obj", SimpleTestDataOne[].class);
  }

  @Test
  public void test_deserialize_null_items() {
    Ason ason = new Ason("{\"array\":[null,null,null]}");
    AsonArray array = ason.get("array");
    assertNotNull(array);
    assertNull(array.get(0));
    assertNull(array.get(1));
    assertNull(array.get(2));
  }

  @Test
  public void test_auto_deserialize_default() {
    Ason ason = new Ason().put("obj", new SimpleTestDataOne());
    SimpleTestDataOne fallback = new SimpleTestDataOne();
    fallback.hi = "fallback";
    SimpleTestDataOne result = ason.get("obj", SimpleTestDataOne.class, fallback);
    assertNotNull(result);
    assertEquals("hello", result.hi);
    result = ason.get("obj2", SimpleTestDataOne.class, fallback);
    assertNotNull(result);
    assertEquals("fallback", result.hi);
  }

  @Test
  public void test_auto_deserialize_primitive() {
    // This happens if you use the variation of get() that takes a class even though it's unnecessary
    Ason ason = new Ason().put("id", 6);
    assertEquals(6, ason.get("id", Integer.class, 0).intValue());
  }

  @Test
  public void test_auto_deserialize_list() {
    Ason ason = new Ason().put("array", 1, 2, 3, 4);
    List<Integer> list = ason.getList("array", Integer.class);
    assertEquals(1, list.get(0).intValue());
    assertEquals(2, list.get(1).intValue());
    assertEquals(3, list.get(2).intValue());
    assertEquals(4, list.get(3).intValue());
  }

  @Test(expected = IllegalStateException.class)
  public void test_auto_deserialize_list_wrong_method() {
    Ason ason = new Ason().put("array", 1, 2, 3, 4);
    ason.get("array", List.class);
  }

  @Test(expected = IllegalStateException.class)
  public void test_get_list_on_object() {
    Ason ason = new Ason().put("value", 1);
    ason.getList("value", Integer.class);
  }

  @Test
  public void test_get_list_null() {
    Ason ason = new Ason().putNull("value");
    assertNull(ason.getList("value", Integer.class));
    assertNull(ason.getList("value2", Integer.class));
  }

  @Test
  public void test_issue10_serialize() {
    Issue10Example data = new Issue10Example();
    data.item = new Object[] {1, 2, 3, 4};

    Ason ason = Ason.serialize(data);
    AsonArray<Integer> array = ason.get("item");
    assertNotNull(array);
    assertEquals(1, array.get(0).intValue());
    assertEquals(2, array.get(1).intValue());
    assertEquals(3, array.get(2).intValue());
    assertEquals(4, array.get(3).intValue());
  }

  @Test
  public void test_issue10_deserialize() {
    Ason ason = new Ason("{\"item\": [1, 2, 3, 4]}");
    Issue10Example result = Ason.deserialize(ason, Issue10Example.class);
    Object[] array = (Object[]) result.item;
    assertTrue(Arrays.equals(new Integer[] {1, 2, 3, 4}, array));
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_deserialize_array_on_non_array() {
    AsonSerializer.get().deserializeArray(new AsonArray(), Integer.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void test_deserialize_array_on_list() {
    AsonSerializer.get().deserializeArray(new AsonArray(), List.class);
  }

  @Test
  public void test_deserialize_all_nulls() {
    AsonArray<Integer> jsonArray = new AsonArray<Integer>().addNull().addNull();
    Integer[] array = AsonSerializer.get().deserializeArray(jsonArray, Integer[].class);
    assertNotNull(array);
    assertNull(array[0]);
    assertNull(array[1]);
  }

  @Test
  public void test_deserialize_all_nulls_to_primitive() {
    AsonArray<Character> jsonArray = new AsonArray<Character>().addNull().addNull();
    char[] array = AsonSerializer.get().deserializeArray(jsonArray, char[].class);
    assertNotNull(array);
    assertEquals('\0', array[0]);
    assertEquals('\0', array[1]);
  }

  @Test
  public void test_deserialize_array_of_arrays() {
    Integer[] one = new Integer[] {1, 2, 3, 4};
    Integer[] two = new Integer[] {5, 6, 7, 8};
    AsonArray<Integer[]> jsonArray = new AsonArray<Integer[]>().add(one, two);
    Integer[][] matrix = AsonSerializer.get().deserializeArray(jsonArray, Integer[][].class);
    assertNotNull(matrix);
    assertArrayEquals(one, matrix[0]);
    assertArrayEquals(two, matrix[1]);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test_deserialize_array_of_lists() {
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

    AsonArray<List<Integer>> jsonArray = new AsonArray<List<Integer>>().add(one, two);
    List<Integer>[] result = AsonSerializer.get().deserializeArray(jsonArray, List[].class);
    assertNotNull(result);

    one = result[0];
    assertEquals(1, one.get(0).intValue());
    assertEquals(2, one.get(1).intValue());
    assertEquals(3, one.get(2).intValue());
    assertEquals(4, one.get(3).intValue());

    two = result[1];
    assertEquals(5, two.get(0).intValue());
    assertEquals(6, two.get(1).intValue());
    assertEquals(7, two.get(2).intValue());
    assertEquals(8, two.get(3).intValue());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test_deserialize_array_of_empty_lists() {
    List<Integer> one = new ArrayList<>(0);
    List<Integer> two = new ArrayList<>(0);

    AsonArray<List<Integer>> jsonArray = new AsonArray<List<Integer>>().add(one, two);
    List<Integer>[] result = AsonSerializer.get().deserializeArray(jsonArray, List[].class);
    assertNotNull(result);
    assertEquals(0, result[0].size());
    assertEquals(0, result[1].size());
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test_deserialize_array_of_null_lists() {
    AsonArray<List<Integer>> jsonArray = new AsonArray<List<Integer>>().addNull().addNull();
    List<Integer>[] result = AsonSerializer.get().deserializeArray(jsonArray, List[].class);
    assertNotNull(result);
    assertNull(result[0]);
    assertNull(result[1]);
  }

  @Test
  public void test_recursive_deserialize() {
    Ason ason =
        new Ason(
            "{\"name\":\"Aidan\",\"power\":\"Flight\",\"_id\":2,\"age\":22,\"spouse\":{\"name\":\"Nina\",\"_id\":6,\"age\":22,\"spouse\":null}}");
    Superhero person = ason.deserialize(Superhero.class, true);
    assertEquals(person.name, "Aidan");
    assertEquals(person.id, 2);
    assertEquals(person.age, 22);
    assertEquals(person.power, "Flight");
  }

  @SuppressWarnings("unused")
  static class Person {

    @AsonName(name = "_id")
    int id;

    String name;
    int age;
    Person spouse;
    @AsonIgnore String gibberish = "Hello, world!";

    Person() {}

    Person(int id, String name, int age) {
      this.id = id;
      this.name = name;
      this.age = age;
    }
  }

  //
  ////// TEST FOR ISSUE #10
  //

  static class Person2 {

    @AsonName(name = "_id")
    int id;

    Person2[] family;

    Person2() {}

    Person2(int id) {
      this();
      this.id = id;
    }
  }

  static class Person3 {

    @AsonName(name = "_id")
    int id;

    List<Person3> family;

    Person3() {
      family = new ArrayList<>(0);
    }

    Person3(int id) {
      this();
      this.id = id;
    }
  }

  @SuppressWarnings("unused")
  private static class Superhero extends Person {

    String power;

    Superhero() {}

    Superhero(int id, String name, int age, String power) {
      super(id, name, age);
      this.power = power;
    }
  }

  @SuppressWarnings("unused")
  private static class EmptyPerson extends Person {

    EmptyPerson() {}

    public EmptyPerson(int id, String name, int age) {
      super(id, name, age);
    }
  }
}
