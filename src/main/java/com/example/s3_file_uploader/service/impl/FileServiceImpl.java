package com.example.s3_file_uploader.service.impl;

import com.example.s3_file_uploader.dto.FileDTO;
import com.example.s3_file_uploader.exception.FileSizeLimitException;
import com.example.s3_file_uploader.mapper.FileMapper;
import com.example.s3_file_uploader.model.File;
import com.example.s3_file_uploader.repository.FileRepository;
import com.example.s3_file_uploader.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    public FileServiceImpl(FileRepository fileRepository) {
        this.fileRepository = fileRepository;
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

        String filePath = UUID.randomUUID().toString() + extension;

        Path uploadDir = Paths.get("files");

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path fullPath = uploadDir.resolve(filePath);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, fullPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("FileServiceImpl::uploadFile: Failed to save file", e);
            throw new RuntimeException("Failed to save file", e);
        }
        FileDTO fileDTO = new FileDTO();
        fileDTO.setFileName(originalFileName);
        fileDTO.setFileUrl(filePath);

        File fileEntity = FileMapper.toEntity(fileDTO);
        fileEntity.setFileUrl(filePath);
        fileRepository.save(fileEntity);

        return FileMapper.toDTO(fileEntity);
    }


    @Override
    public Resource downloadFile(String fileName) {
       try {
            Path file = Paths.get("files/" + fileName);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileNotFoundException("Failed to read file");
            }

       } catch (MalformedURLException | FileNotFoundException e) {
           throw new RuntimeException(e);
       }
    }

    @Override
    public void deleteFile(String id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new NullPointerException("File not found with id: " + id));

        Path path = Paths.get(file.getFileUrl());

        try {
            if (Files.exists(path)) {
                Files.delete(path); // Fiziksel dosyayı sil
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file from disk", e);
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
