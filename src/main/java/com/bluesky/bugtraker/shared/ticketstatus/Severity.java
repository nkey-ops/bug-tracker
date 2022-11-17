package com.bluesky.bugtraker.shared.ticketstatus;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
*  Bug severity is the measure of impact a defect (or bug)
*  can have on the development or functioning.
*  of an application feature when it is being used.
*
 *  Severity of bugs:
*  {@link #LOW},
*  {@link #MINOR},
*  {@link #MAJOR},
*  {@link #CRITICAL}
 *
*/
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Severity {
    /**
     * Won’t result in any noticeable breakdown of the system
     */
    LOW("Low"),
    /**
    * Results in some unexpected or undesired behavior,
    * but not enough to disrupt system function
    */
    MINOR("Minor"),
    /**
    * Capable of collapsing large parts of the system
    */
    MAJOR("Major"),
    /**
    * Capable of triggering complete system shutdown
    */
    CRITICAL("Critical");

    private final String text;
    Severity(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
    public String getName() {
        return name();
    }

    @Override
    public String toString() {
        return text;
    }
}
