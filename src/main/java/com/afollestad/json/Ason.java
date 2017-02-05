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
@SuppressWarnings({"WeakerAccess", "unused", "unchecked", "SameParameterValue"}) public class Ason {

    private JSONObject json;
    private AsonSerializer serializer;
    private boolean loadedMyFields;

    public Ason(JSONObject stock) {
        if (stock == null)
            stock = new JSONObject();
        this.json = stock;
        this.serializer = AsonSerializer.get();
    }

    public Ason() {
        this(new JSONObject());
    }

    public Ason(Map<String, Object> map) {
        this();
        if (map == null) return;
        for (String key : map.keySet()) {
            Object value = map.get(key);
            put(key, value);
        }
    }

    public Ason(String json) {
        try {
            this.json = new JSONObject(json);
        } catch (JSONException e) {
            throw new InvalidJsonException(json, e);
        }
    }

    private Ason putInternal(JSONArray intoArray,
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
            } else if (value instanceof Ason) {
                putInternal(intoArray, intoObject, key, ((Ason) value).toStockJson());
            } else if (value instanceof AsonArray) {
                putInternal(intoArray, intoObject, key, ((AsonArray) value).toStockJson());
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

    public Ason put(String key, Object... values) {
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

    public Ason remove(String key) {
        json.remove(key);
        return this;
    }

    public <T> T get(String key) {
        return get(key, (T) null);
    }

    @SuppressWarnings("unchecked") public <T> T get(String key, T defaultValue) {
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
            return defaultValue;
        } else if (result instanceof JSONObject) {
            result = new Ason((JSONObject) result);
        } else if (result instanceof JSONArray) {
            result = new AsonArray((JSONArray) result);
        }
        try {
            return (T) result;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Did you mean to use get(String, Class<T>)?", e);
        }
    }

    public boolean getBool(String key) {
        return getBool(key, false);
    }

    public boolean getBool(String key, boolean defaultValue) {
        return get(key, defaultValue);
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue) {
        return get(key, defaultValue);
    }

    public short getShort(String key) {
        return getShort(key, (short) 0);
    }

    public short getShort(String key, short defaultValue) {
        return get(key, defaultValue);
    }

    public int getInt(String key) {
        return getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        return get(key, defaultValue);
    }

    public long getLong(String key) {
        return getLong(key, 0L);
    }

    public long getLong(String key, long defaultValue) {
        return get(key, defaultValue);
    }

    public float getFloat(String key) {
        return getFloat(key, 0f);
    }

    public float getFloat(String key, float defaultValue) {
        return get(key, defaultValue);
    }

    public double getDouble(String key) {
        return getDouble(key, 0d);
    }

    public double getDouble(String key, double defaultValue) {
        return get(key, defaultValue);
    }

    public Ason getJsonObject(String key) {
        return get(key, (Ason) null);
    }

    public AsonArray getJsonArray(String key) {
        return get(key, (AsonArray) null);
    }

    public <T> T get(String key, Class<T> cls) {
        return get(key, cls, null);
    }

    @SuppressWarnings("Duplicates") public <T> T get(String key, Class<T> cls, T defaultValue) {
        Object value = get(key, defaultValue);
        if (value == null) {
            return defaultValue;
        } else if (isPrimitive(cls) ||
                cls == JSONObject.class ||
                cls == JSONArray.class ||
                cls == Ason.class ||
                cls == AsonArray.class) {
            return (T) value;
        } else if (cls.isArray()) {
            if (!(value instanceof AsonArray)) {
                throw new IllegalStateException("Expected a AsonArray to convert to " +
                        cls.getName() + ", found " + value.getClass().getName() + ".");
            }
            AsonArray<T> array = (AsonArray<T>) value;
            return (T) AsonSerializer.get().deserializeArray(array, cls.getComponentType());
        } else if (isList(cls)) {
            if (!(value instanceof AsonArray)) {
                throw new IllegalStateException("Expected a AsonArray to convert to " +
                        cls.getName() + ", found " + value.getClass().getName() + ".");
            }
            AsonArray<T> array = (AsonArray<T>) value;
            return (T) AsonSerializer.get().deserializeList(array, cls.getComponentType());
        } else {
            if (!(value instanceof Ason)) {
                throw new IllegalStateException("Expected a Ason to convert to " +
                        cls.getName() + ", found " + value.getClass().getName() + ".");
            }
            Ason object = (Ason) value;
            return AsonSerializer.get().deserialize(object, cls);
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
        return obj instanceof Ason &&
                ((Ason) obj).json.toString().equals(json.toString());
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

    //
    ////// SERIALIZATION
    //

    public static Ason serialize(Object object) {
        return AsonSerializer.get().serialize(object);
    }

    public static <T> AsonArray<T> serializeArray(Object object) {
        return AsonSerializer.get().serializeArray(object);
    }

    public static <T> AsonArray<T> serializeList(List<T> object) {
        return AsonSerializer.get().serializeList(object);
    }

    //
    ////// DESERIALIZATION
    //

    public <T> T deserialize(Class<T> cls) {
        return deserialize(this, cls);
    }

    public static <T> T deserialize(String json, Class<T> cls) {
        if (isJsonArray(json)) {
            AsonArray ason = new AsonArray(json);
            return AsonSerializer.get().deserializeArray(ason, cls);
        } else {
            Ason ason = new Ason(json);
            return AsonSerializer.get().deserialize(ason, cls);
        }
    }

    public static <T> T deserialize(Ason json, Class<T> cls) {
        return AsonSerializer.get().deserialize(json, cls);
    }

    public static <T> T deserialize(AsonArray json, Class<T> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("cls parameter cannot be null.");
        } else if (!cls.isArray()) {
            if (isList(cls)) {
                throw new IllegalStateException("Use Ason.deserializeList() for Lists, not deserialize().");
            }
            throw new IllegalArgumentException(cls.getName() + " is not an array type.");
        }
        return AsonSerializer.get().deserializeArray(json, cls);
    }

    public static <T> List<T> deserializeList(String json, Class<T> cls) {
        AsonArray array = new AsonArray(json);
        return AsonSerializer.get().deserializeList(array, cls);
    }

    public static <T> List<T> deserializeList(AsonArray json, Class<T> cls) {
        return AsonSerializer.get().deserializeList(json, cls);
    }
}
