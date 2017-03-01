package com.afollestad.ason;

import org.json.JSONObject;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.afollestad.ason.Util.*;
import static org.junit.Assert.*;

/** @author Aidan Follestad (afollestad) */
@SuppressWarnings("unused")
public class UtilTest {

  @AsonIgnore Field ignoreYes1;
  Field $jacocoData;
  Field ignoreNo2;
  @SuppressWarnings({"FieldCanBeLocal", "unused", "MismatchedQueryAndUpdateOfCollection"})
  private List<Ason> listField;

  @Test
  public void test_ctor() {
    try {
      new Util();
      assertFalse("Util class shouldn't be constructed!", false);
    } catch (IllegalStateException ignored) {
    }
  }

  @Test
  public void generic_list_type_test() throws Exception {
    listField = new ArrayList<>(0);
    Field field = getClass().getDeclaredField("listField");
    assertEquals(Ason.class, listGenericType(field));
  }

  @Test
  public void test_is_number_true() {
    assertTrue(isNumber("1234"));
    assertTrue(isNumber("67891023231"));
  }

  @Test
  public void test_is_number_false() {
    assertFalse(isNumber("hi"));
    assertFalse(isNumber("@1234"));
    assertFalse(isNumber("1234!%"));
  }

  @Test
  public void test_is_json_array_true() {
    assertTrue(isJsonArray("[]"));
    assertTrue(isJsonArray("   []    "));
  }

  @Test
  public void test_is_json_array_false() {
    assertFalse(isJsonArray(null));
    assertFalse(isJsonArray(""));
    assertFalse(isJsonArray("{}"));
    assertFalse(isJsonArray("  abc"));
  }

  @Test
  public void test_no_default_ctor() {
    try {
      getDefaultConstructor(NoDefaultCtorClass.class);
      assertFalse("No exception thrown for no default constructor!", false);
    } catch (IllegalStateException ignored) {
    }
  }

  @Test
  public void test_cant_access_field() throws Exception {
    DefaultCtorClass instance =
        (DefaultCtorClass) getDefaultConstructor(DefaultCtorClass.class).newInstance();
    Field field = DefaultCtorClass.class.getDeclaredField("hiddenField");
    try {
      setFieldValue(field, instance, "Test");
      assertFalse("No exception was thrown for accessing inaccessible field!", false);
    } catch (RuntimeException ignored) {
    }
  }

  @Test
  public void test_is_null() {
    assertTrue(isNull(null));
    assertTrue(isNull(JSONObject.NULL));
    assertFalse(isNull("Hello"));
    assertFalse(isNull(new Ason()));
    assertFalse(isNull(new AsonArray<>()));
  }

  @Test
  public void test_class_cache_new_instance() {
    ClassCacheEntry<DefaultCtorErrorClass> cacheEntry =
        new ClassCacheEntry<>(DefaultCtorErrorClass.class);
    try {
      cacheEntry.newInstance();
      assertFalse(
          "No exception was thrown when constructing a "
              + "class which throws an error on purpose!",
          false);
    } catch (Throwable ignored) {
    }
  }

  @Test
  public void test_should_ignore() throws Exception {
    assertTrue(shouldIgnore(getClass().getDeclaredField("ignoreYes1")));
    assertTrue(shouldIgnore(getClass().getDeclaredField("$jacocoData")));
    assertFalse(shouldIgnore(getClass().getDeclaredField("ignoreNo2")));
  }

  @SuppressWarnings("unused")
  static class DefaultCtorClass {

    private String hiddenField;

    @SuppressWarnings("unused")
    public DefaultCtorClass() {}
  }

  @SuppressWarnings("unused")
  static class NoDefaultCtorClass {

    @SuppressWarnings("unused")
    public NoDefaultCtorClass(String name) {}
  }

  @SuppressWarnings("unused")
  static class DefaultCtorErrorClass {

    @SuppressWarnings("unused")
    public DefaultCtorErrorClass() {
      throw new IllegalStateException("Here's an exception!");
    }
  }
}
