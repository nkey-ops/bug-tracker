package com.bluesky.bugtraker.shared.ticketstatus;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonKey;

/**
 * The status of the bug
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum  Status {
    TO_FIX("To Fix"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    private final String text;
    Status(String text) {
        this.text = text;
    }
    
    public String getText() {
        return text;
    }
    public String getName() {
        return name();
    }

    @JsonKey()
    @Override
    public String toString() {
        return text;
    }
}
