package com.afollestad.ason;

/** @author Aidan Follestad (afollestad) */
class PerfTestData {

  String name;
  int born;
  String[] props;

  PerfTestData() {
    props =
        new String[] {
          "Hi", "Hey", "Hello", "What is up?", "How is it going?", "Yo", "Hello, world!", "Goodbye"
        };
  }

  PerfTestData(String name, int born) {
    this();
    this.name = name;
    this.born = born;
  }
}
