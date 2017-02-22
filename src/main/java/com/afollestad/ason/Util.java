package com.afollestad.ason;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Aidan Follestad (afollestad)
 */
class Util {

    private Util() {
    }

    static String[] splitPath(String key) {
        List<String> result = new ArrayList<>(4);
        int start = 0;
        for (int i = 0; i < key.length(); i++) {
            if (key.charAt(i) == '.') {
                if (i > 0 && key.charAt(i - 1) == '\\') {
                    continue;
                }
                String entry = key.substring(start, i)
                        .replace("\\.", ".");
                result.add(entry);
                start = i + 1;
            }
        }
        result.add(key.substring(start).replace("\\.", "."));
        return result.toArray(new String[result.size()]);
    }

    private static boolean isNumber(String string) {
        for (char c : string.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    static JSONObject followPath(JSONObject wrapper,
                                 String key,
                                 String[] splitKey,
                                 boolean createMissing) {
        // Get value for the first path key
        Object parent = wrapper.opt(splitKey[0]);
        if (parent != null
                && !(parent instanceof JSONObject)
                && !(parent instanceof JSONArray)) {
            throw new InvalidPathException("First component of key " + key + " refers to " +
                    splitKey[0] + ", which is not an object or array (it's a " +
                    parent.getClass().getName() + ").");
        } else if (parent == null) {
            if (createMissing) {
                parent = new JSONObject();
                wrapper.put(splitKey[0], parent);
            } else {
                throw new InvalidPathException("No object or array found for the first component of key " +
                        key + " (" + splitKey[0] + ").");
            }
        }

        // Loop through following entries
        for (int i = 1; i < splitKey.length - 1; i++) {
            String currentKey = splitKey[i];
            if (currentKey.startsWith("\\$")) {
                // A dollar sign is escaped
                currentKey = currentKey.substring(1);
            } else if (currentKey.startsWith("$")) {
                if (isNumber(currentKey.substring(1))) {
                    // This is an array index key
                    final int index = Integer.parseInt(currentKey.substring(1));
                    Object current = ((JSONArray) parent).opt(index);
                    if (current != null
                            && !(current instanceof JSONObject)
                            && !(current instanceof JSONArray)) {
                        throw new InvalidPathException("Item at index" + i + " of current entry " +
                                " refers to " + currentKey + ", which is not an object or array (it's a " +
                                current.getClass().getName() + ").");
                    } else if (current == null) {
                        if (createMissing) {
                            current = new JSONObject();
                            ((JSONObject) parent).put(currentKey, current);
                        } else {
                            throw new NullPathException("Item at index " + i + " " +
                                    "of current entry refers to a null or out of bounds entry.");
                        }
                    }
                    parent = current;
                    continue;
                }
            }

            // Key is an object name
            Object current = ((JSONObject) parent).opt(currentKey);
            if (current != null
                    && !(current instanceof JSONObject)
                    && !(current instanceof JSONArray)) {
                throw new InvalidPathException("Component " + (i + 1) + " of key " + key +
                        " refers to " + currentKey + ", which is not an object or array (it's a " +
                        current.getClass().getName() + ").");
            } else if (current == null) {
                if (createMissing) {
                    current = new JSONObject();
                    ((JSONObject) parent).put(currentKey, current);
                } else {
                    throw new NullPathException("Item at index " + i + " " +
                            "of current entry refers to a null or out of bounds entry.");
                }
            }
            parent = current;
        }

        return (JSONObject) parent;
    }

    @SuppressWarnings("unchecked") static <T> T getPathValue(
            JSONObject wrapper,
            String key,
            String[] splitKey) {
        if (splitKey.length == 1) {
            return (T) wrapper.get(key);
        }
        JSONObject target = followPath(wrapper, key, splitKey, false);
        return (T) target.opt(splitKey[splitKey.length - 1]);
    }

    @SuppressWarnings("unchecked")
    static <T> T newInstance(
            Class<?> cls,
            Map<Class<?>, Constructor<?>> cache) {
        final Constructor ctor = getDefaultConstructor(cls, cache);
        try {
            return (T) ctor.newInstance();
        } catch (Throwable t) {
            throw new RuntimeException("Failed to instantiate " + cls.getName(), t);
        }
    }

    private static Constructor<?> getDefaultConstructor(
            Class<?> cls,
            Map<Class<?>, Constructor<?>> cache) {
        if (cache != null) {
            Constructor ctor = cache.get(cls);
            if (ctor != null) {
                return ctor;
            }
        }
        final Constructor[] constructorArray = cls.getDeclaredConstructors();
        Constructor constructor = null;
        for (Constructor ct : constructorArray) {
            if (ct.getParameterTypes() != null
                    && ct.getParameterTypes().length != 0) {
                continue;
            }
            constructor = ct;
            if (constructor.getGenericParameterTypes().length == 0) {
                break;
            }
        }
        if (constructor == null) {
            throw new IllegalStateException(
                    "No default constructor found for " + cls.getName());
        }
        constructor.setAccessible(true);
        if (cache != null) {
            cache.put(cls, constructor);
        }
        return constructor;
    }

    static boolean isPrimitive(Class<?> cls) {
        return cls == boolean.class || cls == Boolean.class ||
                cls == double.class || cls == Double.class ||
                cls == float.class || cls == Float.class ||
                cls == short.class || cls == Short.class ||
                cls == int.class || cls == Integer.class ||
                cls == long.class || cls == Long.class ||
                cls == String.class ||
                cls == byte.class || cls == Byte.class ||
                cls == char.class || cls == Character.class;
    }

    static boolean isPrimitive(Object cls) {
        return cls instanceof Boolean ||
                cls instanceof Double ||
                cls instanceof Float ||
                cls instanceof Short ||
                cls instanceof Integer ||
                cls instanceof Long ||
                cls instanceof String ||
                cls instanceof Byte ||
                cls instanceof Character;
    }

    static boolean isList(Class<?> cls) {
        if (cls.equals(List.class))
            return true;
        Class[] is = cls.getInterfaces();
        for (Class i : is)
            if (i.equals(List.class))
                return true;
        return false;
    }

    static boolean shouldIgnore(Field field) {
        return field.getName().startsWith("this$") ||
                field.getAnnotation(AsonIgnore.class) != null;
    }

    static String fieldName(Field field) {
        AsonName annotation = field.getAnnotation(AsonName.class);
        if (annotation != null) {
            return annotation.name();
        }
        return field.getName();
    }

    static void setFieldValue(Field field, Object object, Object value) {
        if (field == null || value == null) {
            return;
        }
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to set the value of " + field.getName() + " in "
                    + object.getClass().getName(), e);
        }
    }

    static boolean isJsonArray(String json) {
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (Character.isAlphabetic(c) || c == '{') {
                return false;
            } else if (c == '[') {
                return true;
            }
        }
        return false;
    }
}
