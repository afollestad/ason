package com.afollestad.ason;

import static com.afollestad.ason.Util.defaultPrimitiveValue;
import static com.afollestad.ason.Util.fieldName;
import static com.afollestad.ason.Util.isList;
import static com.afollestad.ason.Util.isNull;
import static com.afollestad.ason.Util.isPrimitive;
import static com.afollestad.ason.Util.shouldIgnore;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

/** @author Aidan Follestad (afollestad) */
@SuppressWarnings({"unchecked", "WeakerAccess", "unused"})
class AsonSerializer {

  private static AsonSerializer serializer;
  private Map<String, ClassCacheEntry> classCache;

  AsonSerializer() {
    classCache = new HashMap<>(0);
  }

  @NotNull
  public static AsonSerializer get() {
    if (serializer == null) {
      serializer = new AsonSerializer();
    }
    return serializer;
  }

  static List<Field> getDeclaredFields(Class<?> cls, boolean recursive) {
    Field[] declaredFields = cls.getDeclaredFields();
    List<Field> currentFields;
    if (declaredFields == null || declaredFields.length == 0) {
      currentFields = new ArrayList<>(0);
    } else {
      currentFields = new ArrayList<>(declaredFields.length);
      Collections.addAll(currentFields, declaredFields);
    }
    if (!recursive || cls.getSuperclass() == null || cls.getSuperclass() == Object.class) {
      return currentFields;
    }
    currentFields.addAll(getDeclaredFields(cls.getSuperclass(), true));
    return currentFields;
  }

  //
  ////// SERIALIZE
  //

  @Nullable
  public Ason serialize(@Nullable Object object) {
    return serialize(object, false);
  }

  @Nullable
  public Ason serialize(@Nullable Object object, boolean recursive) {
    if (isNull(object)) {
      return null;
    } else if (object instanceof Ason
        || object instanceof AsonArray
        || object instanceof JSONObject
        || object instanceof JSONArray) {
      throw new IllegalArgumentException(
          "You cannot serialize com.afollestad.ason.Ason or com.afollestad.ason.AsonArray.");
    } else if (isPrimitive(object)) {
      throw new IllegalArgumentException(
          "You cannot serialize primitive types (" + object.getClass().getName() + ").");
    } else if (object.getClass().isArray()) {
      throw new IllegalArgumentException(
          "Use com.afollestad.ason.Ason.serialize(Object, Class<?>) to serialize arrays.");
    } else if (isList(object.getClass())) {
      throw new IllegalArgumentException(
          "Use com.afollestad.ason.Ason.serialize(Object, Class<?>) to serialize lists.");
    } else {
      final List<Field> fields = getDeclaredFields(object.getClass(), recursive);
      final Ason ason = new Ason();
      for (Field field : fields) {
        field.setAccessible(true);
        if (shouldIgnore(field)) {
          continue;
        }
        Object result = serializeField(field, object);
        ason.put(fieldName(field), result);
      }
      return ason;
    }
  }

  @Nullable
  public AsonArray serializeArray(@Nullable Object arrayObject) {
    return serializeArray(arrayObject, false);
  }

  @Nullable
  public AsonArray serializeArray(@Nullable Object arrayObject, boolean recursive) {
    if (isNull(arrayObject)) {
      return null;
    }

    Class<?> cls = arrayObject.getClass();
    if (!cls.isArray()) {
      throw new IllegalArgumentException(cls.getName() + " is not an array type.");
    }

    final AsonArray result = new AsonArray<>();
    final int length = Array.getLength(arrayObject);
    if (length == 0) {
      return result;
    }

    for (int i = 0; i < length; i++) {
      Object value = Array.get(arrayObject, i);
      Class<?> itemCls =
          cls.getComponentType() == Object.class && value != null
              ? value.getClass()
              : cls.getComponentType();
      if (isPrimitive(itemCls)) {
        result.add(value);
        continue;
      }
      result.add(serialize(value, recursive));
    }

    return result;
  }

  @Nullable
  public AsonArray serializeList(@Nullable List list) {
    return serializeList(list, false);
  }

  @Nullable
  public AsonArray serializeList(@Nullable List list, boolean recursive) {
    if (isNull(list)) {
      return null;
    } else if (list.isEmpty()) {
      return new AsonArray();
    }

    Class<?> componentType = list.get(0).getClass();
    Object array = Array.newInstance(componentType, list.size());
    for (int i = 0; i < list.size(); i++) {
      Array.set(array, i, list.get(i));
    }
    return serializeArray(array, recursive);
  }

  Object serializeField(final Field field, final Object object) {
    field.setAccessible(true);
    final Object fieldValue;
    try {
      fieldValue = field.get(object);
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }
    if (isNull(fieldValue)) {
      return null;
    }
    if (isPrimitive(fieldValue)
        || fieldValue instanceof JSONObject
        || fieldValue instanceof JSONArray
        || fieldValue instanceof Ason
        || fieldValue instanceof AsonArray) {
      return fieldValue;
    } else if (fieldValue.getClass().isArray()) {
      return serializeArray(fieldValue);
    } else if (isList(fieldValue.getClass())) {
      return serializeList((List) fieldValue);
    } else {
      return serialize(fieldValue);
    }
  }

  //
  ////// DESERIALIZE
  //

  @Nullable
  public <T> T deserialize(@Nullable Ason ason, @NotNull Class<T> cls) {
    return deserialize(ason, cls, false);
  }

  @Nullable
  public <T> T deserialize(@Nullable Ason ason, @NotNull Class<T> cls, boolean recursive) {
    if (isNull(ason)) {
      return null;
    } else if (isPrimitive(cls)) {
      throw new IllegalArgumentException(
          "You cannot deserialize an object to a primitive type (" + cls.getName() + ").");
    } else if (cls == AsonArray.class || cls == JSONArray.class) {
      throw new IllegalArgumentException("You cannot deserialize an object to a JSON array.");
    } else if (cls == Ason.class || cls == JSONObject.class) {
      if (cls == JSONObject.class) {
        return (T) ason.toStockJson();
      }
      return (T) ason;
    }

    ClassCacheEntry<T> cacheEntry = classCache.get(cls.getName());
    if (isNull(cacheEntry)) {
      cacheEntry = new ClassCacheEntry<>(cls, recursive);
      classCache.put(cls.getName(), cacheEntry);
    }
    final T newObject = cacheEntry.newInstance();

    for (String name : cacheEntry.fields(recursive)) {
      final Class<?> type = cacheEntry.fieldType(name);
      if (isPrimitive(type)
          || type == JSONObject.class
          || type == JSONArray.class
          || type == Ason.class
          || type == AsonArray.class) {
        cacheEntry.set(newObject, name, ason.get(name));
      } else if (type.isArray()) {
        AsonArray asonArray = ason.get(name);
        cacheEntry.set(newObject, name, deserializeArray(asonArray, type));
      } else if (isList(type)) {
        AsonArray asonArray = ason.get(name);
        Class<?> listItemType = cacheEntry.listItemType(name);
        cacheEntry.set(newObject, name, deserializeList(asonArray, listItemType));
      } else {
        Object value = ason.get(name);
        if (value instanceof Ason) {
          Ason asonObject = (Ason) value;
          cacheEntry.set(newObject, name, deserialize(asonObject, type));
        } else {
          AsonArray asonArray = (AsonArray) value;
          cacheEntry.set(newObject, name, deserializeArray(asonArray, type));
        }
      }
    }

    return newObject;
  }

  @Nullable
  public <T> T deserializeArray(@Nullable AsonArray json, @NotNull Class<T> cls) {
    return deserializeArray(json, cls, false);
  }

  @Nullable
  public <T> T deserializeArray(
      @Nullable AsonArray json, @NotNull Class<T> cls, boolean recursive) {
    if (isNull(json)) {
      return null;
    } else if (!cls.isArray() && cls != Object.class) {
      if (isList(cls)) {
        throw new IllegalArgumentException(
            "Use com.afollestad.ason.Ason.deserializeList() for Lists, not deserializeArray().");
      }
      throw new IllegalArgumentException(cls.getName() + " is not an array type.");
    }

    final Class<?> component = cls == Object.class ? Object.class : cls.getComponentType();
    final T newArray = (T) Array.newInstance(component, json.size());
    if (json.isEmpty()) {
      return newArray;
    }

    for (int i = 0; i < json.size(); i++) {
      Object item = json.get(i);
      if (isNull(item)) {
        Array.set(newArray, i, defaultPrimitiveValue(component));
        continue;
      }

      final Class<?> itemType = component == Object.class ? item.getClass() : component;
      if (isPrimitive(itemType)) {
        if (itemType == char.class || itemType == Character.class) {
          item = ((String) item).charAt(0);
        } else if (itemType == short.class || itemType == Short.class) {
          item = Short.parseShort(Integer.toString((int) item));
        }
        Array.set(newArray, i, item);
      } else if (itemType.isArray()) {
        AsonArray subArray = (AsonArray) item;
        Array.set(newArray, i, deserializeArray(subArray, component, recursive));
      } else if (isList(itemType)) {
        AsonArray subArray = (AsonArray) item;
        if (subArray.isEmpty()) {
          Array.set(newArray, i, new ArrayList(0));
        } else {
          Class<?> listComponent = subArray.get(0).getClass();
          Array.set(newArray, i, deserializeList(subArray, listComponent, recursive));
        }
      } else if (!(item instanceof Ason)) {
        throw new IllegalStateException(
            "Expected JSON array to contain JSON objects "
                + "to deserialize to "
                + itemType.getName()
                + ", found "
                + item.getClass().getName()
                + " objects instead.");
      } else {
        Ason subObject = (Ason) item;
        Array.set(newArray, i, deserialize(subObject, itemType, recursive));
      }
    }

    return newArray;
  }

  @Nullable
  public <T> List<T> deserializeList(@Nullable AsonArray json, @NotNull Class<T> cls) {
    return deserializeList(json, cls, false);
  }

  @Nullable
  public <T> List<T> deserializeList(
      @Nullable AsonArray json, @NotNull Class<T> cls, boolean recursive) {
    if (isNull(json)) {
      return null;
    } else if (json.isEmpty()) {
      return new ArrayList<>(0);
    }

    Class<?> arrayType = Array.newInstance(cls, 0).getClass();
    Object array = deserializeArray(json, arrayType, recursive);

    int length = Array.getLength(array);
    List<T> result = new ArrayList<>();
    for (int i = 0; i < length; i++) {
      result.add((T) Array.get(array, i));
    }

    return result;
  }
}
