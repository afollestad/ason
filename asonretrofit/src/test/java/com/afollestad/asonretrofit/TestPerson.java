package com.afollestad.asonretrofit;

import com.afollestad.ason.AsonName;

public class TestPerson {

  @AsonName(name = "_id")
  public int id;
  public String name;
  public int age;
}
