package com.afollestad.json;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonPathTest {

    @Test public void builder_test() {
        Json json = new Json()
                .put("person._id", 3)
                .put("person.name", "Aidan")
                .put("person.age", 21);
        String output = "{\"person\":{\"name\":\"Aidan\",\"_id\":3,\"age\":21}}";
        assertEquals(output, json.toString());
    }

    @Test public void from_string_test() {
        String input = "{\"person\":{\"name\":\"Aidan\",\"_id\":3,\"age\":21}}";
        Json json = new Json(input);
        assertEquals(json.size(), 1);
        assertTrue(json.equal("person.name", "Aidan"));
        assertTrue(json.equal("person._id", 3));
        assertTrue(json.equal("person.age", 21));
    }

    @Test public void anon_fields_test() {
        Json json = new Json() {
            @JsonName(name = "person._id") int id = 3;
            @JsonName(name = "person.name") String name = "Aidan";
            @JsonName(name = "person.age") int age = 21;
        };
        assertEquals(json.size(), 1);
        String output = "{\"person\":{\"name\":\"Aidan\",\"_id\":3,\"age\":21}}";
        assertEquals(output, json.toString());
    }
}
