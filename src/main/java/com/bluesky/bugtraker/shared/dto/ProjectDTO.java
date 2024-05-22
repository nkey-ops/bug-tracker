package com.bluesky.bugtraker.shared.dto;

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectDTO {
  private String publicId;
  private String name;
  private UserDTO creator;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ProjectDTO that = (ProjectDTO) o;
    return Objects.equals(publicId, that.publicId)
        && Objects.equals(name, that.name)
        && Objects.equals(creator, that.creator);
  }

  @Override
  public int hashCode() {
    return Objects.hash(publicId, name, creator);
  }
}
