package com.afollestad.ason;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Aidan Follestad (afollestad)
 */
public class AsonSerializeTest {

    static class Person {

        @AsonName(name = "_id") int id;
        String name;
        int age;
        Person spouse;
        @AsonIgnore String gibberish = "Hello, world!";

        public Person() {
        }

        Person(int id, String name, int age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }
    }

    //
    ////// SERIALIZE
    //

    @Test public void test_serialize() {
        Person person = new Person(2, "Aidan", 21);
        person.spouse = new Person(6, "Waverly", 19);
        Ason ason = Ason.serialize(person);
        String expected = "{\"name\":\"Aidan\",\"_id\":2,\"age\":21,\"spouse\":{\"name\":\"Waverly\",\"_id\":6,\"age\":19}}";
        assertEquals(ason.toString(), expected);
    }

    @Test public void test_serialize_array() {
        Person[] people = new Person[]{
                new Person(1, "Aidan", 21),
                new Person(2, "Waverly", 19)
        };
        AsonArray<Person> json = Ason.serializeArray(people);
        String expected = "[{\"name\":\"Aidan\",\"_id\":1,\"age\":21},{\"name\":\"Waverly\",\"_id\":2,\"age\":19}]";
        assertEquals(json.toString(), expected);
    }

    @Test public void test_serialize_list() {
        List<Person> people = new ArrayList<>(2);
        people.add(new Person(1, "Aidan", 21));
        people.add(new Person(2, "Waverly", 19));
        AsonArray<Person> json = Ason.serializeList(people);
        String expected = "[{\"name\":\"Aidan\",\"_id\":1,\"age\":21},{\"name\":\"Waverly\",\"_id\":2,\"age\":19}]";
        assertEquals(json.toString(), expected);
    }

    @Test public void test_put_object_serialize() {
        Ason object = new Ason();
        Person person = new Person(1, "Aidan", 21);
        object.put("person", person);
        assertEquals("{\"person\":{\"name\":\"Aidan\",\"_id\":1,\"age\":21}}", object.toString());
    }

    @Test public void test_put_array_serialize() {
        AsonArray<Person> array = new AsonArray<>();
        Person person = new Person(1, "Aidan", 21);
        array.add(person);
        assertEquals("[{\"name\":\"Aidan\",\"_id\":1,\"age\":21}]", array.toString());
    }

    //
    ////// DESERIALIZE
    //

    @Test public void test_deserialize() {
        String input = "{\"name\":\"Aidan\",\"_id\":2,\"age\":21,\"spouse\":{\"name\":\"Waverly\",\"_id\":6,\"age\":19}}";
        Ason ason = new Ason(input);
        Person person = ason.deserialize(Person.class);
        assertEquals(person.name, "Aidan");
        assertEquals(person.id, 2);
        assertEquals(person.age, 21);

        assertEquals(person.spouse.name, "Waverly");
        assertEquals(person.spouse.id, 6);
        assertEquals(person.spouse.age, 19);
    }

    @Test public void test_deserialize_array() {
        String input = "[{\"name\":\"Aidan\",\"_id\":1,\"age\":21},{\"name\":\"Waverly\",\"_id\":2,\"age\":19}]";
        AsonArray<Person> array = new AsonArray<>(input);
        Person[] people = array.deserialize(Person[].class);

        assertEquals(people[0].name, "Aidan");
        assertEquals(people[0].id, 1);
        assertEquals(people[0].age, 21);

        assertEquals(people[1].name, "Waverly");
        assertEquals(people[1].id, 2);
        assertEquals(people[1].age, 19);
    }

    @Test public void test_deserialize_list() {
        String input = "[{\"name\":\"Aidan\",\"_id\":1,\"age\":21},{\"name\":\"Waverly\",\"_id\":2,\"age\":19}]";
        AsonArray<Person> array = new AsonArray<>(input);
        List<Person> people = array.deserializeList(Person.class);

        assertEquals(people.get(0).name, "Aidan");
        assertEquals(people.get(0).id, 1);
        assertEquals(people.get(0).age, 21);

        assertEquals(people.get(1).name, "Waverly");
        assertEquals(people.get(1).id, 2);
        assertEquals(people.get(1).age, 19);
    }

    @Test public void test_deserialize_string_object() {
        String input = "{\"name\":\"Aidan\",\"_id\":2,\"age\":21,\"spouse\":{\"name\":\"Waverly\",\"_id\":6,\"age\":19}}";
        Person object = Ason.deserialize(input, Person.class);
        assertNotNull(object);
    }

    @Test public void test_deserialize_string_array() {
        String input = "[{\"name\":\"Aidan\",\"_id\":1,\"age\":21},{\"name\":\"Waverly\",\"_id\":2,\"age\":19}]";
        Person[] object = Ason.deserialize(input, Person[].class);
        assertEquals(object.length, 2);
    }

    @Test public void test_deserialize_string_list() {
        String input = "[{\"name\":\"Aidan\",\"_id\":1,\"age\":21},{\"name\":\"Waverly\",\"_id\":2,\"age\":19}]";
        List<Person> object = Ason.deserializeList(input, Person.class);
        assertEquals(object.size(), 2);
    }

    @Test public void test_get_object_deserialize() {
        String input = "{\"person\":{\"name\":\"Aidan\",\"_id\":1,\"age\":21}}";
        Ason ason = new Ason(input);
        Person person = ason.get("person", Person.class);
        assertEquals(person.name, "Aidan");
        assertEquals(person.id, 1);
        assertEquals(person.age, 21);
    }

    @Test public void test_get_array_deserialize() {
        String input = "[{\"name\":\"Aidan\",\"_id\":1,\"age\":21}]";
        AsonArray<Person> json = new AsonArray<>(input);
        Person person = json.get(0, Person.class);
        assertEquals(person.name, "Aidan");
        assertEquals(person.id, 1);
        assertEquals(person.age, 21);
    }
}
