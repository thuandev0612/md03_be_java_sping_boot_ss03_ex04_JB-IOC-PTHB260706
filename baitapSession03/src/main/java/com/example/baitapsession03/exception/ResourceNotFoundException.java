package com.example.baitapsession03.exception;

// Ngoai le rieng cho truong hop khong tim thay ban ghi, khong phu thuoc HTTP.
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
