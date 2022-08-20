package com.bluesky.bugtraker.shared.ticketstatus;

/**
 * The status of the bug
 */
public enum  Status {
    TO_FIX("To Fix"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed");

    private final String name;
    Status(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
