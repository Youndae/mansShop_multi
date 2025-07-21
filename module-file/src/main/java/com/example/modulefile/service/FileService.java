package com.example.modulefile.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3Object;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FileService {

    @Value("#{filePath['file.product.path']}")
    private String filePath;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    private final AmazonS3 amazonS3;
    private final AmazonS3Client amazonS3Client;

    /**
     *
     * @param imageName
     *
     * 개발 환경용 로컬에서 이미지 파일 관리
     */
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

    /**
     *
     * @param imageName
     *
     * S3로부터 파일을 다운받아 InputStreamResource 타입으로 반환.
     * 프론트엔드에서는 blob으로 받아 처리.
     */
    /*public ResponseEntity<InputStreamResource> getImageFileByS3(String imageName) {
        S3Object s3Object = amazonS3.getObject(bucket, imageName);
        InputStreamResource resource = new InputStreamResource(s3Object.getObjectContent());

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imageName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(s3Object.getObjectMetadata().getContentLength())
                .body(resource);
    }*/
}
