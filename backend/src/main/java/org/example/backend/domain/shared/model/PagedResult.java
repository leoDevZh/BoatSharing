package org.example.backend.domain.shared.model;

public record PagedResult<T>(T result, boolean hasNext) {
}
