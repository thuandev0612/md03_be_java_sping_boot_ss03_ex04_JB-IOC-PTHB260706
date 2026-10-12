package com.example.baitapsession03.controller;

import com.example.baitapsession03.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(ApiResponse.failure(exception.getMessage()));
    }

    // Bao gom loi do Spring phat hien truoc khi goi controller:
    // JSON hong, body rong, ID sai kieu, URL khong ton tai, sai HTTP method...
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception exception, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        String message = switch (statusCode.value()) {
            case 400 -> "Yêu cầu không hợp lệ. Hãy kiểm tra ID, kiểu dữ liệu và JSON trong body.";
            case 404 -> "Không tìm thấy đường dẫn được yêu cầu.";
            case 405 -> "Phương thức HTTP không được hỗ trợ tại đường dẫn này.";
            case 406 -> "Định dạng response được yêu cầu không được hỗ trợ.";
            case 415 -> "Content-Type không được hỗ trợ. Hãy gửi application/json.";
            default -> statusCode.is5xxServerError()
                    ? "Có lỗi hệ thống. Vui lòng thử lại sau."
                    : "Không thể xử lý yêu cầu.";
        };
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.addAll(headers);
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(ApiResponse.failure(message), responseHeaders, statusCode);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedError(Exception exception) {
        log.error("Unexpected API error", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.failure("Có lỗi hệ thống. Vui lòng thử lại sau."));
    }
}
