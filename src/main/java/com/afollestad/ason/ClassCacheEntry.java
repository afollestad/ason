package com.afollestad.ason;

import static com.afollestad.ason.AsonSerializer.getDeclaredFields;
import static com.afollestad.ason.Util.fieldName;
import static com.afollestad.ason.Util.getDefaultConstructor;
import static com.afollestad.ason.Util.listGenericType;
import static com.afollestad.ason.Util.setFieldValue;
import static com.afollestad.ason.Util.shouldIgnore;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/** @author Aidan Follestad (afollestad) */
class ClassCacheEntry<T> {

  private boolean gotRecursiveFields;
  private final Class<T> cls;
  private final Constructor<?> ctor;
  private final HashMap<String, Field> fieldMap;
  private final HashMap<String, Class<?>> listGenericTypeMap;

  ClassCacheEntry(Class<T> cls, boolean recursive) {
    this.cls = cls;
    this.fieldMap = new HashMap<>(4);
    this.ctor = getDefaultConstructor(cls);
    this.listGenericTypeMap = new HashMap<>(0);
    invalidateFields(recursive);
  }

  private void invalidateFields(boolean recursive) {
    if (recursive) {
      this.gotRecursiveFields = true;
    }
    final List<Field> fields = getDeclaredFields(cls, recursive);
    for (Field field : fields) {
      field.setAccessible(true);
      if (shouldIgnore(field)) {
        continue;
      }
      final String name = fieldName(field);
      this.fieldMap.put(name, field);
      if (field.getType() == List.class) {
        listGenericTypeMap.put(name, listGenericType(field));
      }
    }
  }

  Set<String> fields(boolean recursive) {
    if (!gotRecursiveFields && recursive) {
      invalidateFields(true);
    }
    return fieldMap.keySet();
  }

  Class<?> fieldType(String name) {
    return fieldMap.get(name).getType();
  }

  Class<?> listItemType(String fieldName) {
    return listGenericTypeMap.get(fieldName);
  }

  @SuppressWarnings("unchecked")
  T newInstance() {
    try {
      return (T) ctor.newInstance();
    } catch (Throwable t) {
      throw new RuntimeException("Failed to instantiate " + cls.getName(), t);
    }
  }

  void set(Object obj, String name, Object value) {
    Field field = fieldMap.get(name);
    setFieldValue(field, obj, value);
  }
}
