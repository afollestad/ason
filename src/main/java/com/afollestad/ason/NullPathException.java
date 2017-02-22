package com.afollestad.ason;

/**
 * @author Aidan Follestad (afollestad)
 */
public class NullPathException extends InvalidPathException {

    NullPathException(String message) {
        super(message);
    }
}
