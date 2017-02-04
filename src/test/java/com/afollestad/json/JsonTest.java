package com.afollestad.json;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JsonTest {

    @Test public void builder_test() {
        Json json = new Json()
                .put("_id", 3)
                .put("name", "Aidan")
                .put("age", 21);
        String output = "{\"name\":\"Aidan\",\"_id\":3,\"age\":21}";
        assertEquals(output, json.toString());
    }

    @Test public void from_string_test() {
        String input = "{\"name\":\"Aidan\",\"_id\":3,\"age\":21}";
        Json json = new Json(input);
        assertEquals(json.size(), 3);
        assertTrue(json.equal("name", "Aidan"));
        assertTrue(json.equal("_id", 3));
        assertTrue(json.equal("age", 21));
    }

    @Test public void anon_fields_test() {
        Json json = new Json() {
            @JsonName(name = "_id") int id = 3;
            String name = "Aidan";
            int age = 21;
        };
        String output = "{\"name\":\"Aidan\",\"_id\":3,\"age\":21}";
        assertEquals(output, json.toString());
    }

    @Test public void test_put_multiple() {
        Json one = new Json().put("greeting", "Hey");
        Json two = new Json().put("greeting", "hello");
        Json json = new Json()
                .put("greetings", one, two);
        assertEquals(json.size(), 1);
        assertEquals(json.toString(), "{\"greetings\":[{\"greeting\":\"Hey\"},{\"greeting\":\"hello\"}]}");
    }
}
