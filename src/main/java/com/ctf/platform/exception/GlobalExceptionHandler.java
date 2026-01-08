package com.ctf.platform.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    public Map<String, Object> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "文件过大！请上传小于 50MB 的文件。");
        return result;
    }
}
