package com.tongji.common.exception;

import lombok.Getter;

/**
 * Business exception.
 *
 * <p>Used to carry a clear {@link ErrorCode} when business validation fails, thrown and converted to HTTP response by global exception handler.</p>
 */
@Getter
public class BusinessException extends RuntimeException {

    /**
     * Business error code used by frontend/caller for stable error branch handling.
     */
    private final ErrorCode errorCode;

    /**
     * Construct exception using error code's default message.
     *
     * @param errorCode Error code (required)
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    /**
     * Construct exception with custom message (error code unchanged).
     *
     * @param errorCode Error code (required)
     * @param message Custom prompt message
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

}
