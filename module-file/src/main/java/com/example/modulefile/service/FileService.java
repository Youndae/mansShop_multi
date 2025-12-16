package com.example.modulefile.service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    ResponseEntity<?> getDisplayImage(String imageName);

    void imageInsert(MultipartFile image, String saveName) throws Exception;

    void deleteImage(String imageName);
}
