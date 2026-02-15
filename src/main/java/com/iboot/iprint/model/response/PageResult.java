package com.iboot.iprint.model.response;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {

    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;

    public static <T> PageResult<T> of(List<T> content, long totalElements, int totalPages, int page, int size) {
        PageResult<T> result = new PageResult<>();
        result.setContent(content);
        result.setTotalElements(totalElements);
        result.setTotalPages(totalPages);
        result.setPage(page);
        result.setSize(size);
        return result;
    }
}
