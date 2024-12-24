package com.gantenx.utils.calculator;

public enum HistogramColor {
    GRAY("gray"),
    GREEN("green"),
    BLUE("blue"),
    RED("red"),
    MAROON("maroon");

    private final String color;

    HistogramColor(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }
}