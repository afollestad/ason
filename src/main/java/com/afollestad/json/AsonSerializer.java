package com.afollestad.json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.afollestad.json.Util.*;

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
                if (shouldIgnore(field)) continue;
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
        if (length == 0) return result;

        for (int i = 0; i < length; i++) {
            Object value = Array.get(arrayObject, i);
            if (isPrimitive(cls.getComponentType())) {
                result.add(value);
                continue;
            }
            result.add(serialize(value));
        }

        return result;
    }

    public AsonArray serializeList(List list) {
        if (list == null || list.isEmpty()) {
            return new AsonArray<>();
        }
        Class<?> componentType = list.get(0).getClass();
        Object array = Array.newInstance(componentType, list.size());
        for (int i = 0; i < list.size(); i++)
            Array.set(array, i, list.get(i));
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
            } else {
                Ason asonObject = ason.get(name);
                setFieldValue(field, newObject, deserialize(asonObject, type));
            }
        }

        return newObject;
    }

    public <T> T deserializeArray(AsonArray json, Class<T> cls) {
        if (json == null) {
            return null;
        } else if (cls == null) {
            throw new IllegalArgumentException("Class<T> parameter is required.");
        } else if (!cls.isArray()) {
            throw new IllegalArgumentException(cls.getName() + " is not an array type.");
        }

        final Class<?> component = cls.getComponentType();
        final T newArray = (T) Array.newInstance(component, json.size());
        if (json.isEmpty()) {
            return newArray;
        }

        for (int i = 0; i < json.size(); i++) {
            if (isPrimitive(component)) {
                Object value = json.get(i);
                if (component == char.class || component == Character.class) {
                    value = ((String) value).charAt(0);
                } else if (component == short.class || component == Short.class) {
                    value = Short.parseShort(Integer.toString((int) value));
                }
                Array.set(newArray, i, value);
            } else if (component.isArray()) {
                AsonArray subArray = (AsonArray) json.get(i);
                Class<?> arrayComponent = component.getComponentType();
                Array.set(newArray, i, deserializeArray(subArray, arrayComponent));
            } else if (isList(component)) {
                AsonArray subArray = (AsonArray) json.get(i);
                if (subArray.isEmpty()) {
                    Array.set(newArray, i, new ArrayList(0));
                } else {
                    Class<?> listComponent = subArray.get(0).getClass();
                    Array.set(newArray, i, deserializeList(subArray, listComponent));
                }
            } else {
                Ason subObject = (Ason) json.get(i);
                Array.set(newArray, i, deserialize(subObject, component));
            }
        }

        return newArray;
    }

    public <T> List<T> deserializeList(AsonArray json, Class<T> cls) {
        if (json == null) {
            return null;
        } else if (cls == null) {
            throw new IllegalArgumentException("Class<T> parameter is required.");
        }

        Class<?> arrayType = Array.newInstance(cls, 0).getClass();
        Object array = deserializeArray(json, arrayType);
        if (array == null) {
            return null;
        }

        int length = Array.getLength(array);
        List<T> result = new ArrayList<>();
        for (int i = 0; i < length; i++)
            result.add((T) Array.get(array, i));

        return result;
    }
}
