package dev.marievski.fooddelivery.common;

public class ApiBadRequestException extends RuntimeException {
    private final String code;
    public ApiBadRequestException(String code, String message) {
        super(message);
        this.code = code;
    }
    public String getCode() { return code; }
}
