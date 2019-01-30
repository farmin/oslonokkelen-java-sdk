package no.oslo.kommune.oslonokkelen.adapter.data;

import java.util.Objects;

abstract class IdentifiedDto implements Comparable<IdentifiedDto> {

  final String id;

  IdentifiedDto(String id) {
    if (id == null || id.isEmpty()) {
      throw new IllegalArgumentException("Id is mandatory");
    }

    this.id = id;
  }

  public final String getId() {
    return id;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    IdentifiedDto that = (IdentifiedDto) o;
    return id.equals(that.id);
  }

  @Override
  public final int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public final int compareTo(IdentifiedDto o) {
    return id.compareTo(o.id);
  }

}
