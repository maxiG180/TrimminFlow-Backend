package com.trimminflow.demo.util;

import com.trimminflow.demo.dto.PageResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class PaginationUtils {

    /**
     * Converts a Spring Data Page<T> to a custom PageResponse<R> using a mapper function.
     *
     * @param page The Spring Data Page object
     * @param mapper Function to convert Entity T to DTO R
     * @param <T> Entity type
     * @param <R> DTO type
     * @return PageResponse containing the mapped content
     */
    public static <T, R> PageResponse<R> createPageResponse(Page<T> page, Function<T, R> mapper) {
        List<R> content = page.getContent().stream()
                .map(mapper)
                .collect(Collectors.toList());

        return new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
