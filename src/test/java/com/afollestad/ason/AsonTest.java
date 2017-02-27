package com.afollestad.ason;

import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.afollestad.ason.Util.isNumber;
import static com.afollestad.ason.Util.listGenericType;
import static org.junit.Assert.*;

public class AsonTest {

    @Test public void invalid_json_test() {
        try {
            new Ason("Hello, world!");
            assertFalse("No exception thrown for invalid JSON!", false);
        } catch (InvalidJsonException ignored) {
        }
    }

    @SuppressWarnings({"FieldCanBeLocal", "unused"}) private List<Ason> listField;

    @Test public void generic_list_type_test() throws Exception {
        listField = new ArrayList<>(0);
        Field field = AsonTest.class.getDeclaredField("listField");
        assertEquals(Ason.class, listGenericType(field));
    }

    @Test public void test_is_number_true() {
        assertTrue(isNumber("1234"));
        assertTrue(isNumber("67891023231"));
    }

    @Test public void test_is_number_false() {
        assertFalse(isNumber("hi"));
        assertFalse(isNumber("@1234"));
        assertFalse(isNumber("1234!%"));
    }

    @Test public void builder_test() {
        Ason ason = new Ason()
                .put("_id", 3)
                .put("name", "Aidan")
                .put("age", 21);
        String output = "{\"name\":\"Aidan\",\"_id\":3,\"age\":21}";
        assertEquals(output, ason.toString());
    }

    @Test public void from_map_test() {
        Map<String, Object> map = new HashMap<>(2);
        map.put("name", "Aidan");
        map.put("born", 1995);
        Ason ason = new Ason(map);
        assertEquals("Aidan", ason.get("name"));
        assertEquals(1995, ason.get("born"));
    }

    @Test public void test_has() {
        Ason ason = new Ason()
                .put("_id", 3)
                .put("name", "Aidan")
                .put("age", 21);
        assertTrue(ason.has("name"));
        assertFalse(ason.has("idk"));
    }

    @Test public void test_is_null() {
        Ason ason = new Ason()
                .put("_id", 3)
                .put("name", (Object[]) null)
                .put("age", 21);
        assertTrue(ason.isNull("name"));
        assertFalse(ason.isNull("age"));
    }

    @Test public void test_remove_key() {
        Ason ason = new Ason()
                .put("_id", 3)
                .put("name", "Aidan")
                .put("age", 21);
        ason.remove("name");
        assertEquals("{\"_id\":3,\"age\":21}", ason.toString());
    }

    @Test public void test_equals() {
        Ason one = new Ason().put("_id", 3);
        Ason two = new Ason().put("_id", 3);
        Ason three = new Ason().put("_id", 4);
        assertEquals(one, two);
        assertNotEquals(one, three);
    }

    @Test public void test_hashcode() {
        Ason ason = new Ason().put("_id", 3);
        assertEquals(ason.hashCode(), ason.toStockJson().hashCode());
    }

    @Test public void from_string_test() {
        String input = "{\"name\":\"Aidan\",\"_id\":3,\"age\":21}";
        Ason ason = new Ason(input);
        assertEquals(ason.size(), 3);
        assertTrue(ason.equal("name", "Aidan"));
        assertTrue(ason.equal("_id", 3));
        assertTrue(ason.equal("age", 21));
        assertEquals(ason.get("non-existent",
                69).intValue(), 69);
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
        assertEquals(ason.toString(), "{\"greetings\":[{\"greeting\":" +
                "\"Hey\"},{\"greeting\":\"hello\"}]}");
    }

    @Test public void test_pretty_print() {
        Ason ason = new Ason()
                .put("name", "Aidan")
                .put("born", 1995);
        assertEquals("{" +
                "\n    \"born\": 1995," +
                "\n    \"name\": \"Aidan\"" +
                "\n}", ason.toString(4));
    }
}
