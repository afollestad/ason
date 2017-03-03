package com.afollestad.ason;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aidan Follestad (afollestad)
 */
class Person {

  int id;
  String name;
  int born;
  String[] props;
  List<Relationship> relationshipList;

  Person() {
    props =
        new String[]{
            "Hi", "Hey", "Hello", "What is up?", "How is it going?", "Yo", "Hello, world!",
            "Goodbye"
        };
    relationshipList = new ArrayList<>();
  }

  Person(int id, String name, int born) {
    this();
    this.id = id;
    this.name = name;
    this.born = born;
  }
}
