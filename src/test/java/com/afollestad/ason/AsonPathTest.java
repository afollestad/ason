package com.afollestad.ason;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AsonPathTest {

    @Test public void test_split_path_no_components() {
        String[] result = Util.splitPath("Hello!");
        assertEquals(1, result.length);
    }

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

    @Test public void builder_index_test_one() {
        Ason ason = new Ason()
                .put("_id", 3)
                .put("name", "Aidan")
                .put("pets.$0", "Kierra")
                .put("pets.$1", "Elijah")
                .put("pets.$2", "Olivia");
        assertEquals("{\"pets\":" +
                "[\"Kierra\",\"Elijah\",\"Olivia\"]," +
                "\"name\":\"Aidan\",\"_id\":3}", ason.toString());
    }

    @Test public void builder_index_test_two() {
        Ason ason = new Ason()
                .put("_id", 3)
                .put("name", "Aidan")
                .put("pets.$0.id", 1)
                .put("pets.$0.name", "Kierra")
                .put("pets.$1.id", 2)
                .put("pets.$1.name", "Elijah")
                .put("pets.$2.id", 3)
                .put("pets.$2.name", "Olivia");
        assertEquals("{\"pets\":[" +
                "{\"name\":\"Kierra\",\"id\":1}," +
                "{\"name\":\"Elijah\",\"id\":2}," +
                "{\"name\":\"Olivia\",\"id\":3}]," +
                "\"name\":\"Aidan\",\"_id\":3}", ason.toString());
    }

    @Test public void builder_index_test_three() {
        Ason ason = new Ason()
                .put("_id", 1)
                .put("people.$0.name", "Aidan")
                .put("people.$0.pets.$0", "Kierra")
                .put("people.$0.pets.$1", "Elijah")
                .put("people.$0.pets.$2", "Olivia");
        assertEquals("{\"_id\":1," +
                "\"people\":[" +
                "{\"pets\":" +
                "[\"Kierra\",\"Elijah\",\"Olivia\"]," +
                "\"name\":\"Aidan\"}" +
                "]" +
                "}", ason.toString());
    }

    @Test public void builder_index_test_four() {
        Ason ason = new Ason()
                .put("_id", 1)
                .put("people.$0.name", "Aidan")
                .put("people.$0.id", 1)
                .put("people.$0.pets.$0.name", "Kierra")
                .put("people.$0.pets.$0.id", 1)
                .put("people.$0.pets.$1.name", "Elijah")
                .put("people.$0.pets.$1.id", 2);
        assertEquals("{\"_id\":1," +
                "\"people\":[" +
                "{\"pets\":" +
                "[{\"name\":\"Kierra\",\"id\":1}," +
                "{\"name\":\"Elijah\",\"id\":2}]," +
                "\"name\":\"Aidan\"," +
                "\"id\":1}]}", ason.toString());
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

    @Test public void test_remove_dot_notation() {
        Ason ason = new Ason()
                .put("_id", 3)
                .put("name", "Aidan")
                .put("age", 21)
                .put("spouse.name", "Waverly")
                .put("spouse.age", 19);
        ason.remove("spouse.age");
        ason.remove("spouse.nonexisting.test"); // nothing should happen here
        assertEquals("{\"name\":\"Aidan\",\"_id\":3,\"age\":21," +
                "\"spouse\":{\"name\":\"Waverly\"}}", ason.toString());
    }

    @Test public void test_remove_index_notation() {
        String input = "{\"group_id\":1,\"title\":\"Hello, world!\"," +
                "\"participants\":[{\"name\":\"Aidan\",\"id\":2}," +
                "{\"name\":\"Waverly\",\"id\":1}]}";
        Ason object = new Ason(input);
        object.remove("participants.$0");

        AsonArray<Ason> participants = object.get("participants");
        assertEquals(participants.size(), 1);
        assertEquals(participants.get(0).get("id"), 1);
        assertEquals(participants.get(0).get("name"), "Waverly");
    }

    @Test public void test_mid_path_null() {
        Ason ason = new Ason()
                .put("person.name", "Aidan")
                .put("person.born", 1995)
                .put("person.spouse.name", "Waverly");
        assertEquals("Aidan", ason.get("person.name"));
        assertNull(ason.get("person.spouse.spouse.age"));
    }
}
