package com.example.modulecache.model.cache;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CacheRequest <T> {

    private T pageDTO;

    private String listType;

    public CacheRequest(T pageDTO) {
        this.pageDTO = pageDTO;
    }
}
