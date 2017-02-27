package com.afollestad.ason;

import com.google.gson.Gson;

import java.util.Map;
import java.util.TreeMap;

import static java.lang.System.out;

/**
 * @author Aidan Follestad (afollestad)
 */
public class PerfTest {

    private static Gson gson;
    private static Map<Integer, Long> asonSerializeTimes;
    private static Map<Integer, Long> asonDeserializeTimes;
    private static Map<Integer, Long> gsonSerializeTimes;
    private static Map<Integer, Long> gsonDeserializeTimes;

    static {
        asonSerializeTimes = new TreeMap<>();
        asonDeserializeTimes = new TreeMap<>();
        gsonSerializeTimes = new TreeMap<>();
        gsonDeserializeTimes = new TreeMap<>();
    }

    private static void asonTest(int sampleSize) {
        long start = System.currentTimeMillis();
        for (int i = 0; i <= sampleSize; i++) {
            final PerfTestData data = new PerfTestData("Aidan", 1995);
            Ason.serialize(data);
        }
        long end = System.currentTimeMillis();
        asonSerializeTimes.put(sampleSize, end - start);

        start = System.currentTimeMillis();
        for (int i = 0; i <= sampleSize; i++) {
            String deserializeInput = "{" +
                    "\"name\": \"Aidan\"," +
                    "\"born\": 1995," +
                    "\"props\": [ " +
                    "\"Hi\",\"Hey\",\"Hello\",\"What is up?\",\"How is it going?\"," +
                    "\"Yo\",\"Hello, world!\",\"Goodbye!\"" +
                    " ]" +
                    "}";
            Ason.deserialize(deserializeInput, PerfTestData.class);
        }
        end = System.currentTimeMillis();
        asonDeserializeTimes.put(sampleSize, end - start);
    }

    private static void gsonTest(int sampleSize) {
        long start = System.currentTimeMillis();
        for (int i = 0; i <= sampleSize; i++) {
            final PerfTestData data = new PerfTestData("Aidan", 1995);
            gson.toJson(data);
        }
        long end = System.currentTimeMillis();
        gsonSerializeTimes.put(sampleSize, end - start);

        start = System.currentTimeMillis();
        for (int i = 0; i <= sampleSize; i++) {
            String deserializeInput = "{" +
                    "\"name\": \"Aidan\"," +
                    "\"born\": 1995," +
                    "\"props\": [ " +
                    "\"Hi\",\"Hey\",\"Hello\",\"What is up?\",\"How is it going?\"," +
                    "\"Yo\",\"Hello, world!\",\"Goodbye!\"" +
                    " ]" +
                    "}";
            gson.fromJson(deserializeInput, PerfTestData.class);
        }
        end = System.currentTimeMillis();
        gsonDeserializeTimes.put(sampleSize, end - start);
    }

    public static void main(String[] args) {
        gson = new Gson();
        out.println("Benchmarking, please wait...");

        long start = System.currentTimeMillis();
        for (int i = 1; i < 6000000; i *= 2) {
            asonTest(i);
            gsonTest(i);
        }
        long end = System.currentTimeMillis();
        long diff = end - start;
        System.out.println("Finished benchmarking in " + diff + "ms (" + ((float) diff / 1000f) + "s)\n");

        out.println("SERIALIZATION...\nSample size | Ason | Gson:");
        for (Integer sampleSize : asonSerializeTimes.keySet()) {
            out.println(sampleSize + " | " +
                    asonSerializeTimes.get(sampleSize) + "ms | "
                    + gsonSerializeTimes.get(sampleSize) + "ms");
        }

        out.println("\nDESERIALIZATION...\nSample size | Ason | Gson:");
        for (Integer sampleSize : asonDeserializeTimes.keySet()) {
            out.println(sampleSize + " | " +
                    asonDeserializeTimes.get(sampleSize) + "ms | "
                    + gsonDeserializeTimes.get(sampleSize) + "ms");
        }
    }
}

