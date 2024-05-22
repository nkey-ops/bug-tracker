package com.bluesky.bugtraker.shared.ticketstatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonKey;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Bug priority refers to how urgently a bug needs to be fixed and eliminated. */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Priority {
  /** Bug can be fixed at a later date. Other, more serious bugs take priority. */
  LOW("Low"),
  /** Bug can be fixed in the normal course of development and testing. */
  MEDIUM("Medium"),
  /**
   * Bug must be resolved at the earliest as it affects the system adversely and renders it unusable
   * until it is resolved.
   */
  HIGH("High");

  private final String text;

  Priority(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public String getName() {
    return name();
  }

  @JsonKey
  @Override
  public String toString() {
    return text;
  }

  @JsonCreator
  public static Priority forValues(
      @JsonProperty("text") String text, @JsonProperty("name") String name) {
    for (Priority priority : Priority.values()) {
      if (priority.getName().equals(name) || priority.getText().equals(text)) return priority;
    }

    return null;
  }
}
