package com.campusgo.api.dto.common;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class PageResponse<T> {
    List<T> list;
    int page;
    int pageSize;
    long total;
}
