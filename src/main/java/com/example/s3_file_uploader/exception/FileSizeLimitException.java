package com.example.s3_file_uploader.exception;

public class FileSizeLimitException extends RuntimeException {
    public FileSizeLimitException(String message) {
        super(message);
    }
}
