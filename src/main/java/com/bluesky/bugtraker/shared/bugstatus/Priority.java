package com.bluesky.bugtraker.shared.bugstatus;

/**
 * Bug priority refers to how urgently a bug needs to be fixed and eliminated.
 *
 */
public enum Priority {
    /**
     * Bug can be fixed at a later date. Other, more serious bugs take priority.
     */
    LOW,
    /**
     * Bug can be fixed in the normal course of development and testing.
     */
    MEDIUM,
    /**
     * Bug must be resolved at the earliest
     * as it affects the system adversely and renders it unusable until it is resolved.
     */
    HIGH

}
