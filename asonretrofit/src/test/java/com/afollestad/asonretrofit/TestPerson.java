package com.afollestad.asonretrofit;

import com.afollestad.ason.AsonName;

public class TestPerson {

  TestPerson() {}

  public TestPerson(int id, String name, int age) {
    this.id = id;
    this.name = name;
    this.age = age;
  }

  @AsonName(name = "_id")
  public int id;

  public String name;
  public int age;
}
