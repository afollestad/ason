package com.afollestad.json;

/**
 * @author Aidan Follestad (afollestad)
 */
public class InvalidJsonException extends IllegalArgumentException {

    InvalidJsonException(String json, Exception inner) {
        super("Invalid JSON: " + json, inner);
    }
}
