package com.afollestad.ason;

/**
 * @author Aidan Follestad (afollestad)
 */
class Relationship {

  int id;
  String name;
  String relationship;

  Relationship() {
  }

  Relationship(int id, String name, String relationship) {
    this();
    this.id = id;
    this.name = name;
    this.relationship = relationship;
  }
}
