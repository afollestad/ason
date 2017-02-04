package com.afollestad.json;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

import static com.afollestad.json.Util.*;

/**
 * @author Aidan Follestad (afollestad)
 */
@SuppressWarnings({"unchecked", "WeakerAccess", "unused"}) public class JsonSerializer {

    private static JsonSerializer serializer;
    private final Map<Class<?>, Constructor<?>> constructorCacheMap;

    JsonSerializer() {
        constructorCacheMap = new HashMap<>(0);
    }

    public static JsonSerializer get() {
        if (serializer == null)
            serializer = new JsonSerializer();
        return serializer;
    }

    //
    ////// SERIALIZE
    //

    public Json serialize(Object object) {
        if (object == null) {
            return null;
        } else if (object instanceof Json || object instanceof JsonArray ||
                object instanceof JSONObject || object instanceof JSONArray) {
            throw new IllegalArgumentException("You cannot serialize Json or JsonArray.");
        } else if (isPrimitive(object)) {
            throw new IllegalArgumentException("You cannot serialize primitive types (" + object.getClass().getName() + ").");
        } else if (object.getClass().isArray()) {
            throw new IllegalArgumentException("Use serializeArray(T[]) to serialize arrays.");
        } else if (isList(object.getClass())) {
            throw new IllegalArgumentException("Use serializeList(List<T>) to serialize lists.");
        } else {
            final Field[] fields = object.getClass().getDeclaredFields();
            final Json json = new Json();
            for (Field field : fields) {
                field.setAccessible(true);
                if (shouldIgnore(field)) continue;
                Object result = serializeField(field, object);
                json.put(fieldName(field), result);
            }
            return json;
        }
    }

    public <T> JsonArray<T> serializeArray(T[] array) {
        JsonArray<T> result = new JsonArray<>();
        if (array == null || array.length == 0) {
            return result;
        }
        for (T object : array) {
            result.add((T) serialize(object));
        }
        return result;
    }

    public <T> JsonArray<T> serializeList(List<T> list) {
        if (list == null || list.isEmpty()) {
            return new JsonArray<>();
        }
        T[] array = (T[]) Array.newInstance(list.get(0).getClass(), list.size());
        for (int i = 0; i < list.size(); i++)
            array[i] = list.get(i);
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
                fieldValue instanceof Json ||
                fieldValue instanceof JsonArray) {
            return fieldValue;
        } else if (fieldValue.getClass().isArray()) {
            return serializeArray((Object[]) fieldValue);
        } else if (isList(fieldValue.getClass())) {
            return serializeList((List) fieldValue);
        } else {
            return serialize(fieldValue);
        }
    }

    //
    ////// DESERIALIZE
    //

    public <T> T deserialize(Json json, Class<T> cls) {
        if (json == null) {
            return null;
        } else if (cls == null) {
            throw new IllegalArgumentException("Class<T> parameter is required.");
        } else if (isPrimitive(cls)) {
            throw new IllegalArgumentException("You cannot deserialize an object to a primitive type (" + cls.getName() + ").");
        } else if (cls == Json.class || cls == JSONObject.class) {
            if (cls == JSONObject.class)
                return (T) json.toStockJson();
            return (T) json;
        } else if (cls == JsonArray.class || cls == JSONArray.class) {
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
                    type == Json.class ||
                    type == JsonArray.class) {
                setFieldValue(field, newObject, json.get(name));
            } else if (type.isArray()) {
                JsonArray jsonArray = json.get(name);
                setFieldValue(field, newObject, deserializeArray(jsonArray, type.getComponentType()));
            } else if (isList(type)) {
                JsonArray jsonArray = json.get(name);
            } else {
                Json jsonObject = json.get(name);
                setFieldValue(field, newObject, deserialize(jsonObject, type));
            }
        }

        return newObject;
    }

    public <T> T[] deserializeArray(JsonArray<T> json, Class<?> cls) {
        if (json == null) {
            return null;
        } else if (cls == null) {
            throw new IllegalArgumentException("Class<T> parameter is required.");
        }

        final T[] newArray = (T[]) Array.newInstance(cls, json.size());
        if (json.isEmpty()) {
            return newArray;
        }

        for (int i = 0; i < json.size(); i++) {
            if (isPrimitive(cls)) {
                newArray[i] = json.get(i);
            } else if (cls.isArray()) {
                JsonArray subArray = (JsonArray) json.get(i);
                newArray[i] = (T) deserializeArray(subArray, cls.getComponentType());
            } else if (isList(cls)) {
                JsonArray subArray = (JsonArray) json.get(i);
                newArray[i] = (T) deserializeList(subArray, cls.getComponentType());
            } else {
                Json subObject = (Json) json.get(i);
                newArray[i] = (T) deserialize(subObject, cls);
            }
        }

        return newArray;
    }

    public <T> List<T> deserializeList(JsonArray<T> json, Class<?> cls) {
        T[] array = deserializeArray(json, cls);
        if (array == null) {
            return null;
        }
        List<T> result = new ArrayList<>(array.length);
        Collections.addAll(result, array);
        return result;
    }
}
