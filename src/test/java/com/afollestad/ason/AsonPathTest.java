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
        String output = "{\"person\":{\"name\":\"Aidan\",\"_id\":3,\"age\":21}}";
        assertEquals(output, ason.toString());
    }
}
