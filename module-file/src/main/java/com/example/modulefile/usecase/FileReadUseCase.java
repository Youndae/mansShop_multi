package com.example.modulefile.usecase;

import com.example.modulefile.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileReadUseCase {

    private final FileService fileService;

    public ResponseEntity<byte[]> getDisplayImage(String imageName) {

        return fileService.getDisplayImage(imageName);
    }
}
