package com.afollestad.ason;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AsonTest {

    @Test public void builder_test() {
        Ason ason = new Ason()
                .put("_id", 3)
                .put("name", "Aidan")
                .put("age", 21);
        String output = "{\"name\":\"Aidan\",\"_id\":3,\"age\":21}";
        assertEquals(output, ason.toString());
    }

    @Test public void from_string_test() {
        String input = "{\"name\":\"Aidan\",\"_id\":3,\"age\":21}";
        Ason ason = new Ason(input);
        assertEquals(ason.size(), 3);
        assertTrue(ason.equal("name", "Aidan"));
        assertTrue(ason.equal("_id", 3));
        assertTrue(ason.equal("age", 21));
        assertEquals(ason.get("non-existent", 69).intValue(), 69);
    }

    @Test public void anon_fields_test() {
        Ason ason = new Ason() {
            @AsonName(name = "_id") int id = 3;
            String name = "Aidan";
            int age = 21;
        };
        String output = "{\"name\":\"Aidan\",\"_id\":3,\"age\":21}";
        assertEquals(output, ason.toString());
    }

    @Test public void test_put_multiple() {
        Ason one = new Ason().put("greeting", "Hey");
        Ason two = new Ason().put("greeting", "hello");
        Ason ason = new Ason()
                .put("greetings", one, two);
        assertEquals(ason.size(), 1);
        assertEquals(ason.toString(), "{\"greetings\":[{\"greeting\":\"Hey\"},{\"greeting\":\"hello\"}]}");
    }
}
