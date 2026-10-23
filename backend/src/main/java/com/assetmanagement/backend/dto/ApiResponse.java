package com.assetmanagement.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.ALWAYS)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private List<FieldErrorResponse> errors;

    public ApiResponse() {
        this.errors = new ArrayList<>();
    }

    public ApiResponse(boolean success, String message, T data, List<FieldErrorResponse> errors) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errors = (errors != null) ? errors : new ArrayList<>();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, new ArrayList<>());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, new ArrayList<>());
    }

    public static <T> ApiResponse<T> error(String message, List<FieldErrorResponse> errors) {
        return new ApiResponse<>(false, message, null, errors);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<FieldErrorResponse> getErrors() {
        return errors;
    }

    public void setErrors(List<FieldErrorResponse> errors) {
        this.errors = errors;
    }
}
