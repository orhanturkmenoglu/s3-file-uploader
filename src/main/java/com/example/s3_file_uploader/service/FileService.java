package com.example.s3_file_uploader.service;

import com.example.s3_file_uploader.dto.FileDTO;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {
    FileDTO uploadFile(MultipartFile file) throws IOException;

    Resource downloadFile(String fileName);

    void deleteFile(String id);

    FileDTO getFileById(String id);
}
