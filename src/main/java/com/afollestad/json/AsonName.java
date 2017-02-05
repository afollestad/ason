package com.afollestad.json;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author Aidan Follestad (afollestad)
 */
@Retention(RUNTIME)
@Target({FIELD})
public @interface AsonName {

    String name();
}
