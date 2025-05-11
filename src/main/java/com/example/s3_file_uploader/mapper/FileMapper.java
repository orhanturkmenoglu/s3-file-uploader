package com.example.s3_file_uploader.mapper;

import com.example.s3_file_uploader.dto.FileDTO;
import com.example.s3_file_uploader.model.File;

public class FileMapper {

    public static FileDTO toDTO(File file) {
        FileDTO fileDTO = new FileDTO();
        fileDTO.setFileUrl(file.getFileUrl());
        fileDTO.setFileName(file.getFileName());
        return fileDTO;
    }

    public static File toEntity(FileDTO fileDTO) {
        File file = new File();
        file.setFileName(fileDTO.getFileName());
        file.setFileUrl(fileDTO.getFileUrl());
        return file;
    }
}
