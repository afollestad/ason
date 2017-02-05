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
public class AsonArray<T> implements Iterable<T> {

    private final JSONArray array;

    public AsonArray() {
        array = new JSONArray();
    }

    public AsonArray(String json) {
        try {
            array = new JSONArray(json);
        } catch (JSONException e) {
            throw new InvalidJsonException(json, e);
        }
    }

    AsonArray(JSONArray internalArray) {
        this.array = internalArray;
    }

    private void putInternal(T object) {
        Object insertObject = null;
        if (object != null) {
            if (isPrimitive(object.getClass()) ||
                    object instanceof JSONObject ||
                    object instanceof JSONArray) {
                insertObject = object;
            } else if (object instanceof Ason) {
                insertObject = ((Ason) object).toStockJson();
            } else if (object instanceof AsonArray) {
                insertObject = ((AsonArray) object).toStockJson();
            } else if (object.getClass().isArray()) {
                insertObject = AsonSerializer.get().serializeArray((Object[]) object);
                if (insertObject != null) insertObject = ((AsonArray) insertObject).toStockJson();
            } else if (isList(object.getClass())) {
                insertObject = AsonSerializer.get().serializeList((List) object);
                if (insertObject != null) insertObject = ((AsonArray) insertObject).toStockJson();
            } else {
                insertObject = AsonSerializer.get().serialize(object);
                if (insertObject != null) insertObject = ((Ason) insertObject).toStockJson();
            }
        }
        array.put(insertObject);
    }

    public AsonArray<T> add(T... objects) {
        for (T obj : objects)
            putInternal(obj);
        return this;
    }

    public T get(int index) {
        try {
            Object value = array.opt(index);
            if (value instanceof JSONObject) {
                value = new Ason((JSONObject) value);
            } else if (value instanceof JSONArray) {
                value = new AsonArray((JSONArray) value);
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
                cls == Ason.class ||
                cls == AsonArray.class) {
            return (T) value;
        } else if (cls.isArray()) {
            if (!(value instanceof JSONArray)) {
                throw new IllegalStateException("Expected a JSONArray to convert to " + cls.getName() + ", didn't find one.");
            }
            AsonArray<T> array = new AsonArray<>((JSONArray) value);
            return (T) AsonSerializer.get().deserializeArray(array, cls.getComponentType());
        } else if (isList(cls)) {
            if (!(value instanceof JSONArray)) {
                throw new IllegalStateException("Expected a JSONArray to convert to " + cls.getName() + ", didn't find one.");
            }
            AsonArray<T> array = new AsonArray<>((JSONArray) value);
            return (T) AsonSerializer.get().deserializeList(array, cls.getComponentType());
        } else {
            if (!(value instanceof JSONObject)) {
                throw new IllegalStateException("Expected a JSONObject to convert to " + cls.getName() + ", didn't find one.");
            }
            Ason object = new Ason((JSONObject) value);
            return AsonSerializer.get().deserialize(object, cls);
        }
    }

    public AsonArray<T> remove(int index) {
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
        if (!(arrayEntry instanceof JSONObject || arrayEntry instanceof Ason)) {
            throw new InvalidPathException("You cannot use equal(int, String, Object) in AsonArray<T> when the array contains primitives (" + arrayEntry.getClass().getName() + ").");
        }
        JSONObject encloser;
        if (arrayEntry instanceof JSONObject) {
            encloser = (JSONObject) arrayEntry;
        } else {
            encloser = ((Ason) arrayEntry).toStockJson();
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

    public <R> R deserialize(Class<?> cls) {
        return (R) Ason.deserialize(this, cls);
    }

    public List<T> deserializeList(Class<T> cls) {
        return Ason.deserializeList(this, cls);
    }
}
