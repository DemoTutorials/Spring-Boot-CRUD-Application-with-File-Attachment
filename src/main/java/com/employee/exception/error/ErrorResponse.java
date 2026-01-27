package com.employee.exception.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse<T> {
    private Boolean success;
    private String message;
    private HttpStatus httpStatus;
    private LocalDateTime localDateTime;
    private T data;
    private String error;

    public ErrorResponse() {
    }

    public ErrorResponse(Boolean success, String message, HttpStatus httpStatus, T data) {
        this.success = success;
        this.message = message;
        this.httpStatus = httpStatus;
        this.localDateTime= LocalDateTime.now();
        this.data = data;
    }

    public ErrorResponse(Boolean success, String message, HttpStatus httpStatus, String error) {
        this.success = success;
        this.message = message;
        this.httpStatus = httpStatus;
        this.localDateTime= LocalDateTime.now();
        this.error = error;
    }

    public static <T>ErrorResponse<T> success(T data,String message){
        return new ErrorResponse<>(true,message,HttpStatus.OK,data);
    }

    public static <T>ErrorResponse<T> create(T data,String message){
        return new ErrorResponse<>(true,message,HttpStatus.CREATED,data);
    }

    public static <T>ErrorResponse<T> error(String message,HttpStatus httpStatus){
        return new ErrorResponse<>(false,message,httpStatus,message);
    }
}
