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

    public ResponseEntity<?> getDisplayImage(String imageName) {
        return fileService.getDisplayImage(imageName);
    }

    /*// dev, test 용 local 저장 기반 메서드
    public ResponseEntity<byte[]> getDisplayImage(String imageName) {

        return fileService.getDisplayImage(imageName);
    }*/

    /*// prod 용 S3 기반 메서드
    public ResponseEntity<InputStreamResource> getImageByS3(String imageName) {

        return fileService.getImageFileByS3(imageName);
    }*/
}
