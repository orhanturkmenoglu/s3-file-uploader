package com.example.s3_file_uploader.controller;


import com.example.s3_file_uploader.dto.FileDTO;
import com.example.s3_file_uploader.service.FileService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;

@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileDTO> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(null); // 400 Bad Request
        }

        try {
            FileDTO fileDTO = fileService.uploadFile(file);
            return ResponseEntity.ok(fileDTO); // 200 OK
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null); // 500 Internal Server Error
        }
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) throws IOException {
        Resource resource = fileService.downloadFile(fileName);

        String contentType ;
        try {
            contentType = Files.probeContentType(resource.getFile().toPath());
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
        }catch (IOException e) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")  // attachment kullanarak dosyayı indirmeye başlatıyoruz
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FileDTO> getFileById(@PathVariable String id) {
        FileDTO dto = fileService.getFileById(id);
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable String id) {
        fileService.deleteFile(id);
        return ResponseEntity.noContent().build();
    }

}
