package com.iboot.iprint.model.request;

import lombok.Data;

@Data
public class PageQuery {

    private Integer page = 1;

    private Integer size = 10;

    private String keyword;
}
