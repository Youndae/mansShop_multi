package com.example.modulefile.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.example.moduleconfig.properties.AwsS3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {

    /*@Value("#{filePath['file.product.path']}")
    private String filePath;*/

    private final AwsS3Properties awsS3Properties;

    private final AmazonS3 amazonS3;

    /**
     *
     * @param imageName
     *
     * 개발 환경용 로컬에서 이미지 파일 관리
     */
    public ResponseEntity<byte[]> getDisplayImage(String imageName) {
//        File file = new File(filePath + imageName);
        File file = new File("" + imageName);
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
    public ResponseEntity<InputStreamResource> getImageFileByS3(String imageName) {
        S3Object s3Object = amazonS3.getObject(awsS3Properties.getBucket(), imageName);
        InputStreamResource resource = new InputStreamResource(s3Object.getObjectContent());

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imageName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(s3Object.getObjectMetadata().getContentLength())
                .body(resource);
    }

    /*public String imageInsert(MultipartFile image) throws Exception{
        StringBuffer sb = new StringBuffer();
        String saveName = sb.append(new SimpleDateFormat("yyyyMMddHHmmss")
                        .format(System.currentTimeMillis()))
                .append(UUID.randomUUID().toString())
                .append(image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf(".")))
                .toString();
        String saveFile = filePath + saveName;

        image.transferTo(new File(saveFile));

        return saveName;
    }*/

    /**
     *
     * @param image
     * S3에 파일 저장
     */
    public String imageInsert(MultipartFile image) throws Exception{
        StringBuffer sb = new StringBuffer();
        String saveName = sb.append(new SimpleDateFormat("yyyyMMddHHmmss")
                        .format(System.currentTimeMillis()))
                .append(UUID.randomUUID().toString())
                .append(image.getOriginalFilename().substring(image.getOriginalFilename().lastIndexOf(".")))
                .toString();

        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(image.getSize());
        objectMetadata.setContentType(image.getContentType());

        try{
            amazonS3.putObject(
                    new PutObjectRequest(
                            awsS3Properties.getBucket(),
                            saveName,
                            image.getInputStream(),
                            objectMetadata
                    )
            );
        }catch (Exception e) {
            log.warn("productImage insert IOException");
            e.printStackTrace();
            throw new NullPointerException();
        }

        return saveName;
    }

    /*public void deleteImage(String imageName) {
        File file = new File(filePath + imageName);

        if(file.exists())
            file.delete();
    }*/

    /**
     *
     *
     * S3 파일 삭제
     */
    public void deleteImage(String imageName) {
        amazonS3.deleteObject(
                new DeleteObjectRequest(awsS3Properties.getBucket(), imageName)
        );
    }
}
