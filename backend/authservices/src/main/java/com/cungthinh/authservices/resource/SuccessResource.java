package com.cungthinh.authservices.resource;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class SuccessResource {
    private String message;
    private Object data;
}
