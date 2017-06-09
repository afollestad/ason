package com.afollestad.ason;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** @author Aidan Follestad (afollestad) */
@Retention(RUNTIME)
@Target({FIELD})
public @interface AsonIgnore {}
