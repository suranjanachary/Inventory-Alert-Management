package com.inventory.alert.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private String errorCode;
    private List<FieldErrorDetail> fieldErrors;

    @Getter
    @Builder
    public static class FieldErrorDetail {
        private String field;
        private String message;
        private Object rejectedValue;
    }
}
