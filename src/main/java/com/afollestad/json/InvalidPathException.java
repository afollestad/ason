package com.afollestad.json;

/**
 * @author Aidan Follestad (afollestad)
 */
public class InvalidPathException extends IllegalArgumentException {

    InvalidPathException(String message) {
        super(message);
    }
}
