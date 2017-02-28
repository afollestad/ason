package com.afollestad.ason;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import static com.afollestad.ason.Util.*;

/**
 * @author Aidan Follestad (afollestad)
 */
@SuppressWarnings({"WeakerAccess", "unused", "unchecked", "SameParameterValue"}) public class Ason {

    private JSONObject json;
    private AsonSerializer serializer;
    private boolean loadedMyFields;

    public Ason(@NotNull JSONObject stock) {
        this.json = stock;
        this.serializer = AsonSerializer.get();
    }

    public Ason() {
        this(new JSONObject());
    }

    public Ason(@NotNull Map<String, Object> map) {
        this();
        for (String key : map.keySet()) {
            Object value = map.get(key);
            put(key, value);
        }
    }

    public Ason(@NotNull String json) {
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
        if (value == null
                || JSONObject.NULL.equals(value)
                || JSONObject.NULL == value) {
            json.put(key, JSONObject.NULL);
            return this;
        } else if (isPrimitive(value) ||
                value instanceof JSONObject ||
                value instanceof JSONArray) {
            if (value instanceof Byte) {
                value = ((Byte) value).intValue();
            } else if (value instanceof Character) {
                value = value.toString();
            }
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
            putInternal(intoArray, intoObject, key, serializer.serializeArray(value));
        } else if (isList(value.getClass())) {
            putInternal(intoArray, intoObject, key, serializer.serializeList((List) value));
        } else {
            putInternal(intoArray, intoObject, key, serializer.serialize(value));
        }
        return this;
    }

    public Ason putNull(@NotNull String key) {
        return put(key, JSONObject.NULL);
    }

    public Ason put(@NotNull String key, @Nullable Object... values) {
        Object insertObject;
        if (values == null || values.length == 1) {
            insertObject = values != null
                    ? values[0] : JSONObject.NULL;
        } else {
            JSONArray newArray = new JSONArray();
            for (Object value : values) {
                putInternal(newArray, null, null, value);
            }
            insertObject = newArray;
        }
        if (key.contains(".")) {
            final String[] splitKey = splitPath(key);
            Object target = followPath(json, key, splitKey, true);
            if (target instanceof JSONArray) {
                JSONArray arrayTarget = (JSONArray) target;
                String indexKey = splitKey[splitKey.length - 1].substring(1);
                int insertIndex = Integer.parseInt(indexKey);
                if (insertIndex > arrayTarget.length() - 1) {
                    arrayTarget.put(insertObject);
                } else {
                    arrayTarget.put(insertIndex, insertObject);
                }
            } else {
                ((JSONObject) target).put(splitKey[splitKey.length - 1], insertObject);
            }
        } else {
            putInternal(null, null, key, insertObject);
        }
        return this;
    }

    public Ason remove(@NotNull String key) {
        String[] splitKey = splitPath(key);
        if (splitKey.length == 1) {
            json.remove(key);
        } else {
            Object followed = followPath(json, key, splitKey, false);
            if (followed instanceof JSONArray) {
                JSONArray followedArray = (JSONArray) followed;
                int insertIndex = Integer.parseInt(splitKey[splitKey.length - 1].substring(1));
                followedArray.remove(insertIndex);
            } else {
                ((JSONObject) followed).remove(splitKey[splitKey.length - 1]);
            }
        }
        return this;
    }

    @Nullable public <T> T get(@NotNull String key) {
        return get(key, (T) null);
    }

    @SuppressWarnings("unchecked") public <T> T get(
            @NotNull String key, @Nullable T defaultValue) {
        Object result;
        if (key.contains(".")) {
            final String[] splitKey = splitPath(key);
            result = getPathValue(json, key, splitKey);
        } else {
            result = json.opt(key);
        }
        if (result == null) {
            return defaultValue;
        } else if (JSONObject.NULL.equals(result)
                || JSONObject.NULL == result) {
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

    public boolean getBool(@NotNull String key) {
        return getBool(key, false);
    }

    public boolean getBool(@NotNull String key, boolean defaultValue) {
        return get(key, defaultValue);
    }

    @Nullable public String getString(@NotNull String key) {
        return getString(key, null);
    }

    @Nullable public String getString(
            @NotNull String key,
            @Nullable String defaultValue) {
        return get(key, defaultValue);
    }

    public short getShort(@NotNull String key) {
        return getShort(key, (short) 0);
    }

    public short getShort(@NotNull String key, short defaultValue) {
        return get(key, defaultValue);
    }

    public int getInt(@NotNull String key) {
        return getInt(key, 0);
    }

    public int getInt(@NotNull String key, int defaultValue) {
        return get(key, defaultValue);
    }

    public long getLong(@NotNull String key) {
        return getLong(key, 0L);
    }

    public long getLong(@NotNull String key, long defaultValue) {
        return get(key, defaultValue);
    }

    public float getFloat(@NotNull String key) {
        return getFloat(key, 0f);
    }

    public float getFloat(@NotNull String key, float defaultValue) {
        return get(key, defaultValue);
    }

    public double getDouble(@NotNull String key) {
        return getDouble(key, 0d);
    }

    public double getDouble(@NotNull String key, double defaultValue) {
        return get(key, defaultValue);
    }

    @Nullable public Character getChar(@NotNull String key) {
        return getChar(key, null);
    }

    @Nullable public Character getChar(@NotNull String key, @Nullable Character defaultValue) {
        String value = getString(key);
        if (value == null || value.length() == 0) {
            return defaultValue;
        }
        return value.charAt(0);
    }

    public byte getByte(@NonNls String key) {
        return getByte(key, (byte) 0);
    }

    public byte getByte(@NonNls String key, byte defaultValue) {
        return (byte) getInt(key, defaultValue);
    }

    public Ason getJsonObject(@NotNull String key) {
        return get(key, (Ason) null);
    }

    public AsonArray getJsonArray(@NotNull String key) {
        return get(key, (AsonArray) null);
    }

    public <T> T get(@NotNull String key, @NotNull Class<T> cls) {
        return get(key, cls, null);
    }

    @SuppressWarnings("Duplicates") public <T> T get(
            @NotNull String key,
            @NotNull Class<T> cls,
            @Nullable T defaultValue) {
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

    public boolean has(@NotNull String key) {
        return get(key) != null;
    }

    public boolean equal(@NotNull String key, @Nullable Object value) {
        Object actual = get(key);
        if (actual == null) {
            return value == null;
        }
        return actual.equals(value);
    }

    public boolean isNull(String key) {
        Object value = get(key);
        return value == null
                || JSONObject.NULL.equals(value)
                || JSONObject.NULL == value;
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
