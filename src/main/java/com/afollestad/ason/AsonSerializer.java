package com.afollestad.ason;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.afollestad.ason.Util.*;

/**
 * @author Aidan Follestad (afollestad)
 */
@SuppressWarnings({"unchecked", "WeakerAccess", "unused"}) class AsonSerializer {

    private static AsonSerializer serializer;
    private final Map<Class<?>, Constructor<?>> constructorCacheMap;

    AsonSerializer() {
        constructorCacheMap = new HashMap<>(0);
    }

    public static AsonSerializer get() {
        if (serializer == null)
            serializer = new AsonSerializer();
        return serializer;
    }

    //
    ////// SERIALIZE
    //

    public Ason serialize(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Ason || object instanceof AsonArray ||
                object instanceof JSONObject || object instanceof JSONArray) {
            throw new IllegalArgumentException("You cannot serialize Ason or AsonArray.");
        } else if (isPrimitive(object)) {
            throw new IllegalArgumentException("You cannot serialize primitive types (" + object.getClass().getName() + ").");
        } else if (object.getClass().isArray()) {
            throw new IllegalArgumentException("Use Ason.serialize(Object, Class<?>) to serialize arrays.");
        } else if (isList(object.getClass())) {
            throw new IllegalArgumentException("Use Ason.serialize(Object, Class<?>) to serialize lists.");
        } else {
            final Field[] fields = object.getClass().getDeclaredFields();
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

    public AsonArray serializeArray(Object arrayObject) {
        if (arrayObject == null) {
            return new AsonArray();
        }

        Class<?> cls = arrayObject.getClass();
        if (cls == null) {
            throw new IllegalArgumentException("cls parameter is required.");
        } else if (!cls.isArray()) {
            throw new IllegalArgumentException(cls.getName() + " is not an array type.");
        }

        final AsonArray result = new AsonArray<>();
        final int length = Array.getLength(arrayObject);
        if (length == 0) {
            return result;
        }

        for (int i = 0; i < length; i++) {
            Object value = Array.get(arrayObject, i);
            Class<?> itemCls = cls.getComponentType() == Object.class
                    && value != null ? value.getClass() : cls.getComponentType();
            if (isPrimitive(itemCls)) {
                result.add(value);
                continue;
            }
            result.add(serialize(value));
        }

        return result;
    }

    @Nullable public AsonArray serializeList(List list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        Class<?> componentType = list.get(0).getClass();
        Object array = Array.newInstance(componentType, list.size());
        for (int i = 0; i < list.size(); i++) {
            Array.set(array, i, list.get(i));
        }
        return serializeArray(array);
    }

    private Object serializeField(final Field field, final Object object) {
        field.setAccessible(true);
        final Object fieldValue;
        try {
            fieldValue = field.get(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        if (fieldValue == null) {
            return null;
        }
        if (isPrimitive(fieldValue) ||
                fieldValue instanceof JSONObject ||
                fieldValue instanceof JSONArray ||
                fieldValue instanceof Ason ||
                fieldValue instanceof AsonArray) {
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

    public <T> T deserialize(Ason ason, Class<T> cls) {
        if (ason == null) {
            return null;
        } else if (cls == null) {
            throw new IllegalArgumentException("Class<T> parameter is required.");
        } else if (isPrimitive(cls)) {
            throw new IllegalArgumentException("You cannot deserialize an object to a primitive type (" + cls.getName() + ").");
        } else if (cls == Ason.class || cls == JSONObject.class) {
            if (cls == JSONObject.class)
                return (T) ason.toStockJson();
            return (T) ason;
        } else if (cls == AsonArray.class || cls == JSONArray.class) {
            throw new IllegalArgumentException("You cannot deserialize an object to a JSON array.");
        }

        final Field[] fields = cls.getDeclaredFields();
        final T newObject = newInstance(cls, constructorCacheMap);

        for (Field field : fields) {
            field.setAccessible(true);
            if (shouldIgnore(field)) continue;
            final String name = fieldName(field);
            final Class<?> type = field.getType();

            if (isPrimitive(type) ||
                    type == JSONObject.class ||
                    type == JSONArray.class ||
                    type == Ason.class ||
                    type == AsonArray.class) {
                setFieldValue(field, newObject, ason.get(name));
            } else if (type.isArray()) {
                AsonArray asonArray = ason.get(name);
                setFieldValue(field, newObject, deserializeArray(asonArray, type.getComponentType()));
            } else if (isList(type)) {
                AsonArray asonArray = ason.get(name);
                Class<?> listItemType = listGenericType(field);
                setFieldValue(field, newObject, deserializeList(asonArray, listItemType));
            } else {
                Object value = ason.get(name);
                if (value instanceof Ason) {
                    Ason asonObject = (Ason) value;
                    setFieldValue(field, newObject, deserialize(asonObject, type));
                } else {
                    AsonArray asonArray = (AsonArray) value;
                    setFieldValue(field, newObject, deserializeArray(asonArray, type));
                }
            }
        }

        return newObject;
    }

    public <T> T deserializeArray(AsonArray json, Class<T> cls) {
        if (json == null) {
            return null;
        } else if (cls == null) {
            throw new IllegalArgumentException("Class<T> parameter is required.");
        } else if (!cls.isArray() && cls != Object.class) {
            throw new IllegalArgumentException(cls.getName() + " is not an array type.");
        }

        final Class<?> component = cls == Object.class ? Object.class : cls.getComponentType();
        final T newArray = (T) Array.newInstance(component, json.size());
        if (json.isEmpty()) {
            return newArray;
        }

        for (int i = 0; i < json.size(); i++) {
            Object item = json.get(i);
            Class<?> itemType = component == Object.class
                    && item != null ? item.getClass() : component;

            if (item == null) {
                Array.set(newArray, i, null);
            } else if (isPrimitive(itemType)) {
                if (itemType == char.class || itemType == Character.class) {
                    item = ((String) item).charAt(0);
                } else if (itemType == short.class || itemType == Short.class) {
                    item = Short.parseShort(Integer.toString((int) item));
                }
                Array.set(newArray, i, item);
            } else if (itemType.isArray()) {
                AsonArray subArray = (AsonArray) item;
                Class<?> arrayComponent = itemType.getComponentType();
                Array.set(newArray, i, deserializeArray(subArray, arrayComponent));
            } else if (isList(itemType)) {
                AsonArray subArray = (AsonArray) item;
                if (subArray.isEmpty()) {
                    Array.set(newArray, i, new ArrayList(0));
                } else {
                    Class<?> listComponent = subArray.get(0).getClass();
                    Array.set(newArray, i, deserializeList(subArray, listComponent));
                }
            } else {
                Ason subObject = (Ason) item;
                Array.set(newArray, i, deserialize(subObject, itemType));
            }
        }

        return newArray;
    }

    public <T> List<T> deserializeList(AsonArray json, Class<T> cls) {
        if (json == null) {
            return null;
        } else if (cls == null) {
            throw new IllegalArgumentException("Class<T> parameter is required.");
        } else if(json.isEmpty()) {
            return new ArrayList<>(0);
        }

        Class<?> arrayType = Array.newInstance(cls, 0).getClass();
        Object array = deserializeArray(json, arrayType);
        if (array == null) {
            return null;
        }

        int length = Array.getLength(array);
        List<T> result = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            result.add((T) Array.get(array, i));
        }

        return result;
    }
}
