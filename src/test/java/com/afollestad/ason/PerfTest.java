package com.afollestad.ason;

import static java.lang.System.out;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Aidan Follestad (afollestad)
 */
public class PerfTest {

  private static Gson gson;
  private static Map<Integer, Long> asonSerializeTimes;
  private static Map<Integer, Long> asonDeserializeTimes;
  private static Map<Integer, Long> gsonSerializeTimes;
  private static Map<Integer, Long> gsonDeserializeTimes;

  private static Person[] people;
  private static String plainJson;

  static {
    asonSerializeTimes = new TreeMap<>();
    asonDeserializeTimes = new TreeMap<>();
    gsonSerializeTimes = new TreeMap<>();
    gsonDeserializeTimes = new TreeMap<>();

    people = new Person[4];

    Person person1 = new Person(1, "Aidan Follestad", 1995);
    person1.relationshipList.add(new Relationship(2, "Waverly Moua", "girlfriend"));
    person1.relationshipList.add(new Relationship(3, "Jeffrey Follestad", "father"));
    person1.relationshipList.add(new Relationship(4, "Natalie Micheal", "mother"));
    people[0] = person1;

    Person person2 = new Person(5, "Waverly Moua", 1997);
    person2.relationshipList.add(new Relationship(6, "Aidan Follestad", "boyfriend"));
    person2.relationshipList.add(new Relationship(7, "Lao Moua", "father"));
    person2.relationshipList.add(new Relationship(8, "Koreena Aroonsavath", "mother"));
    people[1] = person2;

    Person person3 = new Person(9, "Natalie Micheal", 1974);
    person3.relationshipList.add(new Relationship(10, "Aidan Follestad", "son"));
    person3.relationshipList.add(new Relationship(11, "Perry Swenson", "father"));
    person3.relationshipList.add(new Relationship(12, "Joni Campbell", "mother"));
    people[2] = person3;

    Person person4 = new Person(13, "Jeffrey Follestad", 1974);
    person4.relationshipList.add(new Relationship(14, "Aidan Follestad", "son"));
    person4.relationshipList.add(new Relationship(15, "Michael Follestad", "father"));
    person4.relationshipList.add(new Relationship(16, "Jane Bakken", "mother"));
    people[3] = person4;

    plainJson = Ason.serializeArray(people).toString(4);
  }

  private static void asonTest(int sampleSize) {
    long start = System.currentTimeMillis();
    for (int i = 0; i <= sampleSize; i++) {
      Ason.serialize(people[0]);
      Ason.serializeArray(people);
    }
    long end = System.currentTimeMillis();
    asonSerializeTimes.put(sampleSize, end - start);

    start = System.currentTimeMillis();
    for (int i = 0; i <= sampleSize; i++) {
      Ason.deserialize(plainJson, Person[].class);
    }
    end = System.currentTimeMillis();
    asonDeserializeTimes.put(sampleSize, end - start);
  }

  private static void gsonTest(int sampleSize) {
    final Type arrayType = new TypeToken<Person[]>() {
    }.getType();

    long start = System.currentTimeMillis();
    for (int i = 0; i <= sampleSize; i++) {
      gson.toJson(people[0]);
      gson.toJson(people, arrayType);
    }
    long end = System.currentTimeMillis();
    gsonSerializeTimes.put(sampleSize, end - start);

    start = System.currentTimeMillis();
    for (int i = 0; i <= sampleSize; i++) {
      gson.fromJson(plainJson, arrayType);
    }
    end = System.currentTimeMillis();
    gsonDeserializeTimes.put(sampleSize, end - start);
  }

  public static void main(String[] args) {
    gson = new Gson();
    out.println("Benchmarking, please wait...");

    long start = System.currentTimeMillis();
    for (int i = 1; i < 100000; i *= 2) {
      out.print(i + " ");
      asonTest(i);
      gsonTest(i);
    }
    out.println();

    long end = System.currentTimeMillis();
    long diff = end - start;
    System.out.println(
        "Finished benchmarking in " + diff + "ms (" + ((float) diff / 1000f) + "s)\n");

    out.println("SERIALIZATION...\nSample size | Ason | Gson:");
    for (Integer sampleSize : asonSerializeTimes.keySet()) {
      out.println(
          sampleSize
              + " | "
              + asonSerializeTimes.get(sampleSize)
              + " | "
              + gsonSerializeTimes.get(sampleSize));
    }

    out.println("\nDESERIALIZATION...\nSample size | Ason | Gson:");
    for (Integer sampleSize : asonDeserializeTimes.keySet()) {
      out.println(
          sampleSize
              + " | "
              + asonDeserializeTimes.get(sampleSize)
              + " | "
              + gsonDeserializeTimes.get(sampleSize));
    }
  }
}
