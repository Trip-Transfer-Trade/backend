package com.example.module_utility.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Response<T> {
    private final int status;
    private final String message;
    private final T data;

    public Response(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    public static <T> Response<T> success(T data) {
        return new Response<>(200, "success", data);
    }

    public static Response<Void> successWithoutData() {
        return new Response<>(200, "success", null);
    }

    public static <T> Response<T> error(int status, String message) {
        return new Response<>(status, message, null);
    }
}
