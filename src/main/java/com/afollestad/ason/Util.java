package com.afollestad.ason;

import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Aidan Follestad (afollestad)
 */
class Util {

    Util() {
        throw new IllegalStateException("Util shouldn't be constructed!");
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

    static boolean isNumber(String string) {
        for (char c : string.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    @Nullable static Object followPath(
            JSONObject wrapper,
            String key,
            String[] splitKey,
            boolean createMissing) {
        // Get value for the first path key
        Object parent = wrapper.opt(splitKey[0]);
        if (!isNull(parent)
                && !(parent instanceof JSONObject)
                && !(parent instanceof JSONArray)) {
            throw new InvalidPathException("First component of key " + key + " refers to " +
                    splitKey[0] + ", which is not an object or array (it's a " +
                    parent.getClass().getName() + ").");
        } else if (isNull(parent)) {
            if (createMissing) {
                if (splitKey[0].startsWith("$")
                        || (splitKey.length > 1
                        && splitKey[1].startsWith("$"))) {
                    parent = new JSONArray();
                } else {
                    parent = new JSONObject();
                }
                wrapper.put(splitKey[0], parent);
            } else {
                return null;
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
                    if (!(parent instanceof JSONArray)) {
                        throw new InvalidPathException("Cannot use index notation on objects (" +
                                currentKey + " in " + key + ")!");
                    }
                    Object current = ((JSONArray) parent).opt(index);
                    if (isNull(current)) {
                        if (createMissing) {
                            if (i < splitKey.length - 1
                                    && splitKey[i + 1].startsWith("$")) {
                                current = new JSONArray();
                                ((JSONArray) parent).put(current);
                            } else {
                                current = new JSONObject();
                                ((JSONArray) parent).put(current);
                            }
                        } else {
                            return null;
                        }
                    }
                    parent = current;
                    continue;
                }
            }

            // Key is an object name
            Object current = ((JSONObject) parent).opt(currentKey);
            if (!isNull(current)
                    && !(current instanceof JSONObject)
                    && !(current instanceof JSONArray)) {
                return null;
            } else if (isNull(current)) {
                if (createMissing) {
                    if (i < splitKey.length - 1
                            && splitKey[i + 1].startsWith("$")) {
                        current = new JSONArray();
                    } else {
                        current = new JSONObject();
                    }
                    ((JSONObject) parent).put(currentKey, current);
                } else {
                    return null;
                }
            }
            parent = current;
        }

        return parent;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    static <T> T getPathValue(
            JSONObject wrapper,
            String key,
            String[] splitKey) {
        if (splitKey.length == 1) {
            return (T) wrapper.opt(key);
        }
        Object target = followPath(wrapper, key, splitKey, false);
        if (isNull(target)) {
            return null;
        }

        final String lastKey = splitKey[splitKey.length - 1];
        if (target instanceof JSONObject) {
            return (T) ((JSONObject) target).opt(lastKey);
        } else if (target instanceof JSONArray
                && lastKey.startsWith("$")
                && isNumber(lastKey.substring(1))) {
            int index = Integer.parseInt(lastKey.substring(1));
            return (T) ((JSONArray) target).opt(index);
        } else {
            throw new InvalidPathException("Cannot get a value from a JSONArray using a name key (" + lastKey + ").");
        }
    }

    static Constructor<?> getDefaultConstructor(Class<?> cls) {
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
        for (Class i : is) {
            if (i.equals(List.class)) {
                return true;
            }
        }
        return false;
    }

    static boolean shouldIgnore(Field field) {
        return field.getName().startsWith("this$")
                || field.getName().equals("$jacocoData") // used with Jacoco testing
                || field.getAnnotation(AsonIgnore.class) != null;
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

    static boolean isJsonArray(@Nullable String json) {
        if (json == null) {
            return false;
        }
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

    static Class<?> listGenericType(Field field) {
        ParameterizedType stringListType = (ParameterizedType) field.getGenericType();
        return (Class<?>) stringListType.getActualTypeArguments()[0];
    }

    static boolean isNull(@Nullable Object value) {
        return value == null
                || JSONObject.NULL.equals(value)
                || JSONObject.NULL == value;
    }
}
