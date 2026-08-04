package com.getpayin.paylink.internal;

/** One request field: its snake_case wire name and whether it is signed. */
public record FieldSpec(String name, boolean signed) {

    public static FieldSpec of(String name) {
        return new FieldSpec(name, true);
    }

    public static FieldSpec unsigned(String name) {
        return new FieldSpec(name, false);
    }
}
