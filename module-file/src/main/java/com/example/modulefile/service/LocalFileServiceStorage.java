package com.example.modulefile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Service
@Profile({"dev", "test"})
@RequiredArgsConstructor
@Slf4j
public class LocalFileServiceStorage implements FileService {

    @Value("#{filePath['file.product.path']}")
    private String filePath;

    /**
     *
     * @param imageName
     *
     * 개발 환경용 로컬에서 이미지 파일 관리
     */
    @Override
    public ResponseEntity<byte[]> getDisplayImage(String imageName) {
        File file = new File(filePath + imageName);
        ResponseEntity<byte[]> result = null;

        try {
            HttpHeaders header = new HttpHeaders();

            String contentType = "";
            if(imageName.endsWith(".png"))
                contentType = "image/png";
            else if(imageName.endsWith(".jpg") || imageName.endsWith(".jpeg"))
                contentType = "image/jpeg";
            else if (imageName.endsWith(".gif"))
                contentType = "image/gif";
            else
                contentType = "application/octet-stream";

            header.add("Content-Type", contentType);

            try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file))) {
                byte[] imageBytes = bis.readAllBytes();
                result = new ResponseEntity<>(imageBytes, header, HttpStatus.OK);
            }
        }catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public void imageInsert(MultipartFile image, String saveName) throws Exception{
        String saveFile = filePath + saveName;

        image.transferTo(new File(saveFile));
    }

    @Override
    public void deleteImage(String imageName) {
        File file = new File(filePath + imageName);

        if(file.exists())
            file.delete();
    }
}
