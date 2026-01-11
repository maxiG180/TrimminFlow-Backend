package com.trimminflow.demo.util;

import com.trimminflow.demo.dto.PageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaginationUtilsTest {

    @Test
    void createPageResponse_WithData_ReturnsCorrectResponse() {
        // Given
        List<String> content = Arrays.asList("Item1", "Item2", "Item3");
        Page<String> page = new PageImpl<>(content, PageRequest.of(0, 10), 3);

        // When
        PageResponse<String> result = new PageResponse<>(
                content,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());

        // Then
        assertNotNull(result);
        assertEquals(content, result.getContent());
        assertEquals(0, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void createPageResponse_WithEmptyPage_ReturnsEmptyResponse() {
        // Given
        Page<String> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        // When
        PageResponse<String> result = new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages());

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
    }

    @Test
    void pageResponse_PreservesData() {
        // Given
        List<String> content = Arrays.asList("A", "B", "C");

        // When
        PageResponse<String> result = new PageResponse<>(content, 1, 3, 10L, 4);

        // Then
        assertEquals(3, result.getContent().size());
        assertEquals(1, result.getPageNumber());
        assertEquals(3, result.getPageSize());
        assertEquals(10L, result.getTotalElements());
        assertEquals(4, result.getTotalPages());
    }
}
