package com.afollestad.ason;

import static com.afollestad.ason.Util.defaultPrimitiveValue;
import static com.afollestad.ason.Util.getDefaultConstructor;
import static com.afollestad.ason.Util.isJsonArray;
import static com.afollestad.ason.Util.isList;
import static com.afollestad.ason.Util.isNull;
import static com.afollestad.ason.Util.isNumber;
import static com.afollestad.ason.Util.listGenericType;
import static com.afollestad.ason.Util.setFieldValue;
import static com.afollestad.ason.Util.shouldIgnore;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;
import org.junit.Test;

/** @author Aidan Follestad (afollestad) */
@SuppressWarnings("unused")
public class UtilTest {

  @AsonIgnore Field ignoreYes1;
  Field $jacocoData;
  Field ignoreNo2;

  @SuppressWarnings({"FieldCanBeLocal", "unused", "MismatchedQueryAndUpdateOfCollection"})
  private List<Ason> listField;

  @Test(expected = IllegalStateException.class)
  public void test_ctor() {
    new Util();
  }

  @Test
  public void test_is_list_cls() {
    assertFalse(isList(null));
    assertFalse(isList(int.class));
    assertFalse(isList(SimpleTestDataOne.class));
    assertTrue(isList(List.class));
    assertTrue(isList(ArrayList.class));
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

  @Test(expected = IllegalStateException.class)
  public void test_no_default_ctor() {
    getDefaultConstructor(NoDefaultCtorClass.class);
  }

  @Test(expected = RuntimeException.class)
  public void test_cant_access_field() throws Exception {
    DefaultCtorClass instance =
        (DefaultCtorClass) getDefaultConstructor(DefaultCtorClass.class).newInstance();
    Field field = DefaultCtorClass.class.getDeclaredField("hiddenField");
    setFieldValue(field, instance, "Test");
  }

  @Test
  public void test_is_null() {
    assertTrue(isNull(null));
    assertTrue(isNull(JSONObject.NULL));
    assertFalse(isNull("Hello"));
    assertFalse(isNull(new Ason()));
    assertFalse(isNull(new AsonArray<>()));
  }

  @Test(expected = Throwable.class)
  public void test_class_cache_new_instance() {
    ClassCacheEntry<DefaultCtorErrorClass> cacheEntry =
        new ClassCacheEntry<>(DefaultCtorErrorClass.class, false);
    cacheEntry.newInstance();
  }

  @Test
  public void test_should_ignore() throws Exception {
    assertTrue(shouldIgnore(getClass().getDeclaredField("ignoreYes1")));
    assertTrue(shouldIgnore(getClass().getDeclaredField("$jacocoData")));
    assertFalse(shouldIgnore(getClass().getDeclaredField("ignoreNo2")));
  }

  @Test
  public void test_default_primitive() throws Exception {
    assertEquals(false, defaultPrimitiveValue(boolean.class));
    assertEquals(0d, defaultPrimitiveValue(double.class));
    assertEquals(0f, defaultPrimitiveValue(float.class));
    assertEquals((short) 0, defaultPrimitiveValue(short.class));
    assertEquals(0, defaultPrimitiveValue(int.class));
    assertEquals(0L, defaultPrimitiveValue(long.class));
    assertEquals((byte) 0, defaultPrimitiveValue(byte.class));
    assertEquals('\0', defaultPrimitiveValue(char.class));
    assertNull(defaultPrimitiveValue(String.class));
    assertNull(defaultPrimitiveValue(Character.class));
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
