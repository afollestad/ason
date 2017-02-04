package com.afollestad.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.afollestad.json.Util.isList;
import static com.afollestad.json.Util.isPrimitive;

/**
 * @author Aidan Follestad (afollestad)
 */
@SuppressWarnings({"unchecked", "WeakerAccess", "unused"})
public class JsonArray<T> implements Iterable<T> {

    private final JSONArray array;

    public JsonArray() {
        array = new JSONArray();
    }

    public JsonArray(String json) {
        try {
            array = new JSONArray(json);
        } catch (JSONException e) {
            throw new InvalidJsonException(json, e);
        }
    }

    JsonArray(JSONArray internalArray) {
        this.array = internalArray;
    }

    private void putInternal(T object) {
        Object insertObject = null;
        if (object != null) {
            if (isPrimitive(object.getClass()) ||
                    object instanceof JSONObject ||
                    object instanceof JSONArray) {
                insertObject = object;
            } else if (object instanceof Json) {
                insertObject = ((Json) object).toStockJson();
            } else if (object instanceof JsonArray) {
                insertObject = ((JsonArray) object).toStockJson();
            } else if (object.getClass().isArray()) {
                insertObject = JsonSerializer.get().serializeArray((Object[]) object);
                if (insertObject != null) insertObject = ((JsonArray) insertObject).toStockJson();
            } else if (isList(object.getClass())) {
                insertObject = JsonSerializer.get().serializeList((List) object);
                if (insertObject != null) insertObject = ((JsonArray) insertObject).toStockJson();
            } else {
                insertObject = JsonSerializer.get().serialize(object);
                if (insertObject != null) insertObject = ((Json) insertObject).toStockJson();
            }
        }
        array.put(insertObject);
    }

    public JsonArray<T> add(T... objects) {
        for (T obj : objects)
            putInternal(obj);
        return this;
    }

    public T get(int index) {
        try {
            Object value = array.opt(index);
            if (value instanceof JSONObject) {
                value = new Json((JSONObject) value);
            } else if (value instanceof JSONArray) {
                value = new JsonArray((JSONArray) value);
            }
            return (T) value;
        } catch (ClassCastException e) {
            throw new IllegalStateException("Did you mean to use get(int, Class<T>)?", e);
        }
    }

    public T get(int index, Class<T> cls) {
        Object value = array.opt(index);
        if (value == null) {
            return null;
        } else if (isPrimitive(cls) ||
                cls == JSONObject.class ||
                cls == JSONArray.class ||
                cls == Json.class ||
                cls == JsonArray.class) {
            return (T) value;
        } else if (cls.isArray()) {
            if (!(value instanceof JSONArray)) {
                throw new IllegalStateException("Expected a JSONArray to convert to " + cls.getName() + ", didn't find one.");
            }
            JsonArray<T> array = new JsonArray<>((JSONArray) value);
            return (T) JsonSerializer.get().deserializeArray(array, cls.getComponentType());
        } else if (isList(cls)) {
            if (!(value instanceof JSONArray)) {
                throw new IllegalStateException("Expected a JSONArray to convert to " + cls.getName() + ", didn't find one.");
            }
            JsonArray<T> array = new JsonArray<>((JSONArray) value);
            return (T) JsonSerializer.get().deserializeList(array, cls.getComponentType());
        } else {
            if (!(value instanceof JSONObject)) {
                throw new IllegalStateException("Expected a JSONObject to convert to " + cls.getName() + ", didn't find one.");
            }
            Json object = new Json((JSONObject) value);
            return JsonSerializer.get().deserialize(object, cls);
        }
    }

    public JsonArray<T> remove(int index) {
        array.remove(index);
        return this;
    }

    public boolean equal(int index, Object value) {
        T actual = get(index);
        if (actual == null) {
            return value == null;
        }
        return actual.equals(value);
    }

    public boolean equal(int index, String path, Object value) {
        if (index >= size()) {
            throw new IndexOutOfBoundsException("Index " + index + " is out of bounds for this array of size " + size() + ".");
        }
        T arrayEntry = get(index);
        if (!(arrayEntry instanceof JSONObject || arrayEntry instanceof Json)) {
            throw new InvalidPathException("You cannot use equal(int, String, Object) in JsonArray<T> when the array contains primitives (" + arrayEntry.getClass().getName() + ").");
        }
        JSONObject encloser;
        if (arrayEntry instanceof JSONObject) {
            encloser = (JSONObject) arrayEntry;
        } else {
            encloser = ((Json) arrayEntry).toStockJson();
        }
        Object pathValue = Util.getPathValue(encloser, path, path.split("\\."));
        if (pathValue == null) {
            return value == null;
        }
        return pathValue.equals(value);
    }

    public int size() {
        return array.length();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    private List<T> toList() {
        List<T> list = new ArrayList<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            list.add((T) array.opt(i));
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
}
