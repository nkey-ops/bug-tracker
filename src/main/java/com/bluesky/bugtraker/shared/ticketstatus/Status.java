package com.bluesky.bugtraker.shared.ticketstatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonKey;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The status of the bug */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Status {
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

  @JsonKey
  @Override
  public String toString() {
    return text;
  }

  @JsonCreator
  public static Status forValues(
      @JsonProperty("text") String text, @JsonProperty("name") String name) {
    for (Status status : Status.values()) {
      if (status.getText().equals(text) && status.getName().equals(name)) return status;
    }

    return null;
  }
}
