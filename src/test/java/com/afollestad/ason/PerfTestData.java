package com.afollestad.ason;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aidan Follestad (afollestad)
 */
public class PerfTestData {

    public String name;
    public int born;
    public List<String> props;

    public PerfTestData() {
        props = new ArrayList<>(8);
        props.add("Hi");
        props.add("Hey");
        props.add("Hello");
        props.add("What is up?");
        props.add("How is it going?");
        props.add("Yo");
        props.add("Hello, world!");
        props.add("Goodbye");
    }

    public PerfTestData(String name, int born) {
        this();
        this.name = name;
        this.born = born;
    }
}
