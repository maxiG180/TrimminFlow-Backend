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
    void toPageResponse_WithData_ReturnsCorrectPageResponse() {
        // Given
        List<String> content = Arrays.asList("Item1", "Item2", "Item3");
        Page<String> page = new PageImpl<>(content, PageRequest.of(0, 10), 3);

        // When
        PageResponse<String> result = PaginationUtils.toPageResponse(page);

        // Then
        assertNotNull(result);
        assertEquals(content, result.getContent());
        assertEquals(0, result.getPageNumber());
        assertEquals(10, result.getPageSize());
        assertEquals(3, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void toPageResponse_WithEmptyPage_ReturnsEmptyResponse() {
        // Given
        Page<String> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        // When
        PageResponse<String> result = PaginationUtils.toPageResponse(page);

        // Then
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
    }

    @Test
    void toPageResponse_WithMultiplePages_ReturnsCorrectTotalPages() {
        // Given
        List<String> content = Arrays.asList("Item1", "Item2");
        Page<String> page = new PageImpl<>(content, PageRequest.of(1, 2), 10); // Page 2 of 5

        // When
        PageResponse<String> result = PaginationUtils.toPageResponse(page);

        // Then
        assertEquals(2, result.getContent().size());
        assertEquals(1, result.getPageNumber());
        assertEquals(2, result.getPageSize());
        assertEquals(10, result.getTotalElements());
        assertEquals(5, result.getTotalPages());
    }

    @Test
    void toPageResponse_WithLastPage_ReturnsCorrectData() {
        // Given
        List<String> content = List.of("Item10");
        Page<String> page = new PageImpl<>(content, PageRequest.of(9, 1), 10); // Last page

        // When
        PageResponse<String> result = PaginationUtils.toPageResponse(page);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals(9, result.getPageNumber());
        assertEquals(10, result.getTotalPages());
        assertEquals(10, result.getTotalElements());
    }

    @Test
    void toPageResponse_WithDifferentPageSizes_WorksCorrectly() {
        // Given - Page size 20
        List<String> content = Arrays.asList("Item1", "Item2", "Item3");
        Page<String> page = new PageImpl<>(content, PageRequest.of(0, 20), 3);

        // When
        PageResponse<String> result = PaginationUtils.toPageResponse(page);

        // Then
        assertEquals(20, result.getPageSize());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void toPageResponse_PreservesContentOrder() {
        // Given
        List<String> content = Arrays.asList("First", "Second", "Third");
        Page<String> page = new PageImpl<>(content, PageRequest.of(0, 10), 3);

        // When
        PageResponse<String> result = PaginationUtils.toPageResponse(page);

        // Then
        assertEquals("First", result.getContent().get(0));
        assertEquals("Second", result.getContent().get(1));
        assertEquals("Third", result.getContent().get(2));
    }
}
