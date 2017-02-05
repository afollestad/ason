package com.afollestad.json;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AsonArrayTest {

    private AsonArray<Ason> array;

    @Before public void setup() {
        array = new AsonArray<Ason>()
                .add(new Ason()
                        .put("_id", 1)
                        .put("name", "Aidan")
                        .put("attrs.priority", 2))
                .add(new Ason()
                        .put("_id", 2)
                        .put("name", "Waverly")
                        .put("attrs.priority", 1));
    }

    @Test public void builder_test() {
        String expected = "[{\"name\":\"Aidan\",\"_id\":1,\"attrs\":{\"priority\":2}},{\"name\":\"Waverly\",\"_id\":2,\"attrs\":{\"priority\":1}}]";
        assertEquals(array.toString(), expected);
    }

    @Test public void from_string_test() {
        assertEquals(array.size(), 2);

        assertTrue(array.equal(0, "name", "Aidan"));
        assertTrue(array.equal(0, "_id", 1));
        assertTrue(array.equal(0, "attrs.priority", 2));

        assertTrue(array.equal(1, "name", "Waverly"));
        assertTrue(array.equal(1, "_id", 2));
        assertTrue(array.equal(1, "attrs.priority", 1));
    }
}
