package com.afollestad.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import static com.afollestad.json.Util.*;

/**
 * @author Aidan Follestad (afollestad)
 */
@SuppressWarnings({"WeakerAccess", "unused", "unchecked"}) public class Json {

    private JSONObject json;
    private JsonSerializer serializer;
    private boolean loadedMyFields;

    public Json(JSONObject stock) {
        if (stock == null)
            stock = new JSONObject();
        this.json = stock;
        this.serializer = JsonSerializer.get();
    }

    public Json() {
        this(new JSONObject());
    }

    public Json(Map<String, Object> map) {
        this();
        if (map == null) return;
        for (String key : map.keySet()) {
            Object value = map.get(key);
            put(key, value);
        }
    }

    public Json(String json) {
        try {
            this.json = new JSONObject(json);
        } catch (JSONException e) {
            throw new InvalidJsonException(json, e);
        }
    }

    private Json putInternal(JSONArray intoArray,
                             JSONObject intoObject,
                             String key,
                             Object value) {
        invalidateLoadedFields();
        try {
            if (value == null) {
                return this;
            } else if (isPrimitive(value) ||
                    value instanceof JSONObject ||
                    value instanceof JSONArray) {
                if (intoArray != null) {
                    intoArray.put(value);
                } else if (intoObject != null) {
                    intoObject.put(key, value);
                } else {
                    json.put(key, value);
                }
            } else if (value instanceof Json) {
                putInternal(intoArray, intoObject, key, ((Json) value).toStockJson());
            } else if (value instanceof JsonArray) {
                putInternal(intoArray, intoObject, key, ((JsonArray) value).toStockJson());
            } else if (value.getClass().isArray()) {
                putInternal(intoArray, intoObject, key, serializer.serializeArray((Object[]) value));
            } else if (isList(value.getClass())) {
                putInternal(intoArray, intoObject, key, serializer.serializeList((List) value));
            } else {
                putInternal(intoArray, intoObject, key, serializer.serialize(value));
            }
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
        return this;
    }

    public Json put(String key, Object... values) {
        if (key == null)
            throw new IllegalArgumentException("Key cannot be null.");
        Object insertObject;
        if (values == null || values.length == 1) {
            insertObject = values != null ? values[0] : null;
        } else {
            JSONArray newArray = new JSONArray();
            for (Object value : values) {
                putInternal(newArray, null, null, value);
            }
            insertObject = newArray;
        }
        if (key.contains(".")) {
            final String[] splitKey = key.split("\\.");
            JSONObject target = Util.followPath(json, key, splitKey, true);
            target.put(splitKey[splitKey.length - 1], insertObject);
        } else {
            putInternal(null, null, key, insertObject);
        }
        return this;
    }

    public Json remove(String key) {
        json.remove(key);
        return this;
    }

    @SuppressWarnings("unchecked") public <T> T get(String key) {
        if (key == null)
            throw new IllegalArgumentException("Key cannot be null.");
        Object result;
        if (key.contains(".")) {
            final String[] splitKey = key.split("\\.");
            result = getPathValue(json, key, splitKey);
        } else {
            result = json.opt(key);
        }
        if (result == null) {
            return null;
        } else if (result instanceof JSONObject) {
            result = new Json((JSONObject) result);
        } else if (result instanceof JSONArray) {
            result = new JsonArray((JSONArray) result);
        }
        try {
            return (T) result;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Did you mean to use get(String, Class<T>)?", e);
        }
    }

    public boolean getBool(String key) {
        return (Boolean) get(key);
    }

    public String getString(String key) {
        return (String) get(key);
    }

    public short getShort(String key) {
        return (Short) get(key);
    }

    public int getInt(String key) {
        return (Integer) get(key);
    }

    public long getLong(String key) {
        return (Long) get(key);
    }

    public float getFloat(String key) {
        return (Float) get(key);
    }

    public double getDouble(String key) {
        return (Double) get(key);
    }

    @SuppressWarnings("Duplicates") public <T> T get(String key, Class<T> cls) {
        Object value = get(key);
        if (value == null) {
            return null;
        } else if (isPrimitive(cls) ||
                cls == JSONObject.class ||
                cls == JSONArray.class ||
                cls == Json.class ||
                cls == JsonArray.class) {
            return (T) value;
        } else if (cls.isArray()) {
            if (!(value instanceof JsonArray)) {
                throw new IllegalStateException("Expected a JsonArray to convert to " +
                        cls.getName() + ", found " + value.getClass().getName() + ".");
            }
            JsonArray<T> array = (JsonArray<T>) value;
            return (T) JsonSerializer.get().deserializeArray(array, cls.getComponentType());
        } else if (isList(cls)) {
            if (!(value instanceof JsonArray)) {
                throw new IllegalStateException("Expected a JsonArray to convert to " +
                        cls.getName() + ", found " + value.getClass().getName() + ".");
            }
            JsonArray<T> array = (JsonArray<T>) value;
            return (T) JsonSerializer.get().deserializeList(array, cls.getComponentType());
        } else {
            if (!(value instanceof Json)) {
                throw new IllegalStateException("Expected a Json to convert to " +
                        cls.getName() + ", found " + value.getClass().getName() + ".");
            }
            Json object = (Json) value;
            return JsonSerializer.get().deserialize(object, cls);
        }
    }

    public boolean has(String key) {
        return get(key) != null;
    }

    public boolean equal(String key, Object value) {
        if (key == null)
            throw new IllegalArgumentException("Key cannot be null.");
        Object actual = get(key);
        if (actual == null) {
            return value == null;
        }
        return actual.equals(value);
    }

    public boolean isNull(String key) {
        return get(key) == null;
    }

    @Override public int hashCode() {
        return json.hashCode();
    }

    public int size() {
        invalidateLoadedFields();
        return json.length();
    }

    public JSONObject toStockJson() {
        return json;
    }

    private void invalidateLoadedFields() {
        if (loadedMyFields) {
            return;
        }
        loadedMyFields = true;

        Field[] fields = getClass().getDeclaredFields();
        for (Field f : fields) {
            if (Modifier.isPrivate(f.getModifiers()) || shouldIgnore(f)) {
                continue;
            }

            f.setAccessible(true);
            String name = fieldName(f);

            try {
                put(name, f.get(this));
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Override public boolean equals(Object obj) {
        return obj instanceof Json &&
                ((Json) obj).json.toString().equals(json.toString());
    }

    @Override public String toString() {
        invalidateLoadedFields();
        return json.toString();
    }

    public String toString(int indentSpaces) {
        invalidateLoadedFields();
        try {
            return json.toString(indentSpaces);
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }
}
