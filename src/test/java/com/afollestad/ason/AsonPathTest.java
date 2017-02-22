package com.afollestad.ason;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AsonPathTest {

    @Test public void builder_test() {
        Ason ason = new Ason()
                .put("person._id", 3)
                .put("person.name", "Aidan")
                .put("person.age", 21);

        assertEquals(3, ason.get("person._id"));
        assertEquals("Aidan", ason.get("person.name"));
        assertEquals(21, ason.get("person.age"));

        String output = "{\"person\":{\"name\":\"Aidan\",\"_id\":3,\"age\":21}}";
        assertEquals(output, ason.toString());
    }

    @Test public void from_string_test() {
        String input = "{\"person\":{\"name\":\"Aidan\",\"_id\":3,\"age\":21}}";
        Ason ason = new Ason(input);

        assertEquals(ason.size(), 1);
        assertTrue(ason.equal("person.name", "Aidan"));
        assertTrue(ason.equal("person._id", 3));
        assertTrue(ason.equal("person.age", 21));
    }

    @Test public void anon_fields_test() {
        Ason ason = new Ason() {
            @AsonName(name = "person._id") int id = 3;
            @AsonName(name = "person.name") String name = "Aidan";
            @AsonName(name = "person.age") int age = 21;
        };

        assertEquals(ason.size(), 1);
        assertEquals(3, ason.get("person._id"));
        assertEquals("Aidan", ason.get("person.name"));
        assertEquals(21, ason.get("person.age"));

        String output = "{\"person\":{\"name\":\"Aidan\",\"_id\":3,\"age\":21}}";
        assertEquals(output, ason.toString());
    }

    @Test public void array_get_path_test() {
        String input = "[{\"body\":\"Hello, world\",\"sender\":{\"name\":\"Aidan\",\"id\":2}}," +
                "{\"body\":\"Hello, world\",\"sender\":{\"name\":\"Waverly\",\"id\":1}}," +
                "{\"body\":\"Hello, world\",\"sender\":{\"name\":\"Jeff\",\"id\":3}}]";
        AsonArray array = new AsonArray(input);

        assertEquals("Waverly", array.get(1, "sender.name"));
        assertEquals(3, array.get(2, "sender.id"));
    }

    @Test public void test_escape_period() {
        String input = "{\"files\":{\"test.txt\":\"Hello, world!\"}}";
        Ason object = new Ason(input);
        assertEquals("Hello, world!", object.get("files.test\\.txt"));
    }

    @Test public void test_index_notation() {
        String input = "{\"group_id\":1,\"title\":\"Hello, world!\"," +
                "\"participants\":[{\"name\":\"Aidan\",\"id\":2}," +
                "{\"name\":\"Waverly\",\"id\":1}]}";
        Ason object = new Ason(input);

        assertEquals("Waverly", object.get("participants.$1.name"));
        assertEquals(2, object.get("participants.$0.id"));
    }

    @Test public void test_escape_dollarsign() {
        String input = "{\"participants\":{\"$1\":{\"name\":\"Waverly\"}}}";
        Ason object = new Ason(input);
        assertEquals("Waverly", object.get("participants.\\$1.name"));
    }
}
