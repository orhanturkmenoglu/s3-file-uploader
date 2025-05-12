package com.example.s3_file_uploader.service.impl;

import com.example.s3_file_uploader.dto.FileDTO;
import com.example.s3_file_uploader.exception.FileSizeLimitException;
import com.example.s3_file_uploader.mapper.FileMapper;
import com.example.s3_file_uploader.model.File;
import com.example.s3_file_uploader.repository.FileRepository;
import com.example.s3_file_uploader.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    private final FileRepository fileRepository;

    private final List<String> contentType = List.of(
            "image/png",
            "image/jpeg",
            "image/jpg",
            "image/gif"
    );

    @Value("${spring.application.bucket.name}")
    private String bucketName;

    private final S3Client s3Client;

    public FileServiceImpl(FileRepository fileRepository, S3Client s3Client) {
        this.fileRepository = fileRepository;
        this.s3Client = s3Client;
    }

    @Override
    public FileDTO uploadFile(MultipartFile file) throws IOException {
        // PATH , FİLES FİLE
        long fileSize = file.getSize();
        long maxFileSize = 5 * 1024 * 1024;  // 5MB

        if (file.isEmpty()) {
            log.error("FileServiceImpl::uploadFile: File is empty");
            throw new FileNotFoundException("File is empty");
        }

        if (fileSize > maxFileSize) {
            log.error("FileServiceImpl::uploadFile: File size is too large");
            throw new FileSizeLimitException("File size is too large");
        }


        if (!contentType.contains(file.getContentType())) {
            log.error("FileServiceImpl::uploadFile: File type is not supported");
            throw new IllegalArgumentException("File type is not supported");
        }

        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));

        String fileKey = UUID.randomUUID().toString() + extension;

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (Exception e) {
            log.error("FileServiceImpl::uploadFile: Failed to upload file to S3", e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }

        FileDTO fileDTO = new FileDTO();
        fileDTO.setFileName(originalFileName);
        fileDTO.setFileUrl(fileKey);

        File fileEntity = FileMapper.toEntity(fileDTO);
        fileEntity.setFileUrl(fileKey);
        fileRepository.save(fileEntity);

        return FileMapper.toDTO(fileEntity);
    }


    @Override
    public Resource downloadFile(String fileName) {
        try {
            // S3'ten dosyayı al
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            ResponseBytes<GetObjectResponse> responseBytes = s3Client.getObjectAsBytes(request);

            // Dosyanın içerik türünü al
            String contentType = "application/octet-stream";  // Default fallback content type
            try {
                HeadObjectRequest headRequest = HeadObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .build();
                HeadObjectResponse headResponse = s3Client.headObject(headRequest);
                contentType = headResponse.contentType(); // Content type
            } catch (Exception e) {
                log.warn("Could not retrieve content type for file: " + fileName);
                throw new RuntimeException("Could not retrieve content type for file: " + fileName, e);
            }

            // ByteArrayResource oluştur
            ByteArrayResource resource = new ByteArrayResource(responseBytes.asByteArray()) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            };

            return resource;  // Dosya başarıyla indirildi
        } catch (Exception e) {
            log.error("FileServiceImpl::downloadFile: Failed to download file from S3", e);
            throw new RuntimeException("Failed to download file from S3", e);
        }
    }

    @Override
    public void deleteFile(String id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new NullPointerException("File not found with id: " + id));

        String fileKey = file.getFileUrl();

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            log.error("FileServiceImpl::deleteFile: Failed to delete file from S3", e);
            throw new RuntimeException("Failed to delete file from S3", e);
        }

        fileRepository.deleteById(id); // Veritabanından sil
    }

    @Override
    public FileDTO getFileById(String id) {
        File file = fileRepository.findById(id).orElseThrow(
                () -> new NullPointerException("File not found")
        );

        return FileMapper.toDTO(file);
    }


}
