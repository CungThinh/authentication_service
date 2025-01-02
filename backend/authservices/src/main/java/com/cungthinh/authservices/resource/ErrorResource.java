package com.cungthinh.authservices.resource;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ErrorResource {
    private String message;
    private Map<String, String> errors;
}
