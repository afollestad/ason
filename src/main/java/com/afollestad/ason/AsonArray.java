package com.afollestad.ason;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.afollestad.ason.Util.*;

/** @author Aidan Follestad (afollestad) */
@SuppressWarnings({"unchecked", "WeakerAccess", "unused", "SameParameterValue"})
public class AsonArray<T> implements Iterable<T> {

  private final JSONArray array;

  public AsonArray() {
    array = new JSONArray();
  }

  public AsonArray(@Nullable String json) {
    if (json == null) {
      array = new JSONArray();
      return;
    }
    try {
      array = new JSONArray(json);
    } catch (JSONException e) {
      throw new InvalidJsonException(json, e);
    }
  }

  AsonArray(@NotNull JSONArray internalArray) {
    this.array = internalArray;
  }

  private void putInternal(Object object) {
    Object insertObject;
    if (object == null || JSONObject.NULL.equals(object) || JSONObject.NULL == object) {
      insertObject = JSONObject.NULL;
    } else {
      if (isPrimitive(object.getClass())
          || object instanceof JSONObject
          || object instanceof JSONArray) {
        insertObject = object;
      } else if (object instanceof Ason) {
        insertObject = ((Ason) object).toStockJson();
      } else if (object instanceof AsonArray) {
        insertObject = ((AsonArray) object).toStockJson();
      } else if (object.getClass().isArray()) {
        insertObject = AsonSerializer.get().serializeArray(object);
        if (insertObject != null) {
          insertObject = ((AsonArray) insertObject).toStockJson();
        }
      } else if (isList(object.getClass())) {
        insertObject = AsonSerializer.get().serializeList((List) object);
        if (insertObject != null) {
          insertObject = ((AsonArray) insertObject).toStockJson();
        }
      } else {
        insertObject = AsonSerializer.get().serialize(object);
        if (insertObject != null) {
          insertObject = ((Ason) insertObject).toStockJson();
        }
      }
    }
    array.put(insertObject);
  }

  public AsonArray<T> addNull() {
    putInternal(JSONObject.NULL);
    return this;
  }

  public AsonArray<T> add(@Nullable T... objects) {
    if (objects != null) {
      for (T obj : objects) {
        putInternal(obj);
      }
    } else {
      putInternal(JSONObject.NULL);
    }
    return this;
  }

  @Nullable
  public T get(int index) {
    Object value = array.opt(index);
    if (value instanceof JSONObject) {
      value = new Ason((JSONObject) value);
    } else if (value instanceof JSONArray) {
      value = new AsonArray((JSONArray) value);
    }
    return (T) value;
  }

  @Nullable
  public Ason getJsonObject(int index) {
    if (index < 0 || index > array.length() - 1) {
      throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for this array!");
    }
    JSONObject object = array.optJSONObject(index);
    if (Util.isNull(object)) {
      return null;
    }
    return new Ason(object);
  }

  @Nullable
  public AsonArray getJsonArray(int index) {
    if (index < 0 || index > array.length() - 1) {
      throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for this array!");
    }
    JSONArray ary = array.optJSONArray(index);
    if (Util.isNull(ary)) {
      return null;
    }
    return new AsonArray(ary);
  }

  public T get(int index, Class<T> cls) {
    return get(index, null, cls);
  }

  public T get(int index, @Nullable String path) {
    return get(index, path, null);
  }

  public T get(int index, @Nullable String path, Class<T> cls) {
    if (index < 0 || index > array.length() - 1) {
      throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for this array!");
    }
    Object value = array.opt(index);
    if (path != null) {
      if (Util.isNull(value)) {
        return null;
      }
      if (!(value instanceof JSONObject) && !(value instanceof Ason)) {
        throw new IllegalArgumentException(
            "Cannot get from an AsonArray using a "
                + "path when array items are not objects (they're probably primitives).");
      }
      if (value instanceof JSONObject) {
        value = getPathValue((JSONObject) value, path, splitPath(path));
      } else {
        value = getPathValue(((Ason) value).toStockJson(), path, splitPath(path));
      }
      if (Util.isNull(value)) {
        return null;
      }
      cls = (Class<T>) value.getClass();
    }

    if (Util.isNull(value)) {
      return null;
    }
    if (cls == null) {
      throw new IllegalArgumentException("cls parameter cannot be null!");
    }

    if (isPrimitive(cls)
        || cls == JSONObject.class
        || cls == JSONArray.class
        || cls == Ason.class
        || cls == AsonArray.class) {
      return (T) value;
    } else if (cls.isArray()) {
      if (!(value instanceof JSONArray)) {
        throw new IllegalStateException(
            "Expected a JSONArray to convert to " + cls.getName() + ", didn't find one.");
      }
      AsonArray<T> array = new AsonArray<>((JSONArray) value);
      return (T) AsonSerializer.get().deserializeArray(array, cls.getComponentType());
    } else if (isList(cls)) {
      if (!(value instanceof JSONArray)) {
        throw new IllegalStateException(
            "Expected a JSONArray to convert to " + cls.getName() + ", didn't find one.");
      }
      AsonArray<T> array = new AsonArray<>((JSONArray) value);
      return (T) AsonSerializer.get().deserializeList(array, cls.getComponentType());
    } else {
      if (!(value instanceof JSONObject)) {
        throw new IllegalStateException(
            "Expected a JSONObject to convert to " + cls.getName() + ", didn't find one.");
      }
      Ason object = new Ason((JSONObject) value);
      return AsonSerializer.get().deserialize(object, cls);
    }
  }

  public AsonArray<T> remove(int index) {
    if (index < 0 || index > array.length() - 1) {
      throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for this array!");
    }
    array.remove(index);
    return this;
  }

  public boolean equal(int index, @Nullable T value) {
    T actual = get(index);
    if (Util.isNull(actual)) {
      return Util.isNull(value);
    }
    return actual.equals(value);
  }

  public boolean equal(int index, String path, @Nullable Object value) {
    if (index < 0 || index > array.length() - 1) {
      throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for this array!");
    }
    T arrayEntry = get(index);
    if (Util.isNull(arrayEntry)) {
      return Util.isNull(value);
    }
    if (!(arrayEntry instanceof JSONObject || arrayEntry instanceof Ason)) {
      throw new InvalidPathException(
          "You cannot use equal(int, String, "
              + "Object) in AsonArray<T> when the array contains primitives ("
              + arrayEntry.getClass().getName()
              + ").");
    }
    JSONObject encloser;
    if (arrayEntry instanceof JSONObject) {
      encloser = (JSONObject) arrayEntry;
    } else {
      encloser = ((Ason) arrayEntry).toStockJson();
    }
    Object pathValue = getPathValue(encloser, path, splitPath(path));
    if (Util.isNull(pathValue)) {
      return Util.isNull(value);
    }
    return pathValue.equals(value);
  }

  public int size() {
    return array.length();
  }

  public boolean isEmpty() {
    return size() == 0;
  }

  @NotNull
  public List<T> toList() {
    List<T> list = new ArrayList<>(array.length());
    for (int i = 0; i < array.length(); i++) {
      Object val = array.opt(i);
      if (Util.isNull(val)) {
        val = null;
      }
      list.add((T) val);
    }
    return list;
  }

  public JSONArray toStockJson() {
    return array;
  }

  @Override
  public Iterator<T> iterator() {
    return toList().iterator();
  }

  @Override
  public String toString() {
    return array.toString();
  }

  public String toString(int indentSpaces) {
    try {
      return array.toString(indentSpaces);
    } catch (JSONException e) {
      throw new IllegalStateException(e);
    }
  }

  public <R> R deserialize(Class<?> cls) {
    return (R) Ason.deserialize(this, cls);
  }

  public List<T> deserializeList(Class<T> cls) {
    return Ason.deserializeList(this, cls);
  }
}
