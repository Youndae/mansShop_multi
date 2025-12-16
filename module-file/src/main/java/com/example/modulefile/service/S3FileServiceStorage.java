package com.example.modulefile.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.example.moduleconfig.properties.AwsS3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Profile("prod")
@RequiredArgsConstructor
@Slf4j
public class S3FileServiceStorage implements FileService {

    private final AwsS3Properties awsS3Properties;

    private final AmazonS3 amazonS3;

    @Override
    public ResponseEntity<InputStreamResource> getDisplayImage(String imageName) {
        S3Object s3Object = amazonS3.getObject(awsS3Properties.getBucket(), imageName);
        InputStreamResource resource = new InputStreamResource(s3Object.getObjectContent());

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + imageName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(s3Object.getObjectMetadata().getContentLength())
                .body(resource);
    }

    @Override
    public void imageInsert(MultipartFile image, String saveName) throws Exception {
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
    }

    @Override
    public void deleteImage(String imageName) {
        amazonS3.deleteObject(
                new DeleteObjectRequest(awsS3Properties.getBucket(), imageName)
        );
    }
}
