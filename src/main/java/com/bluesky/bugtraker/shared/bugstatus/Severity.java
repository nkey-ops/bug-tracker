package com.bluesky.bugtraker.shared.bugstatus;

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
public enum Severity {
    /**
     * Won’t result in any noticeable breakdown of the system
     */
    LOW,
    /**
    * Results in some unexpected or undesired behavior,
    * but not enough to disrupt system function
    */
    MINOR,
    /**
    * Capable of collapsing large parts of the system
    */
    MAJOR,
    /**
    * Capable of triggering complete system shutdown
    */
    CRITICAL
}
