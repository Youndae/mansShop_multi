package com.example.modulefile.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.UUID;

@Service
@Slf4j
public class FileDomainService {

    public String setImageSaveName(MultipartFile image) throws Exception{
        if(image == null || image.isEmpty()) {
            log.error("file image is null or empty");
            throw new NullPointerException("image is null or empty");
        }

        StringBuffer sb = new StringBuffer();

        return sb.append(
                    new SimpleDateFormat("yyyyMMddHHmmss")
                        .format(System.currentTimeMillis())
                )
                .append(UUID.randomUUID().toString())
                .append(
                        image
                                .getOriginalFilename()
                                .substring(
                                        image
                                                .getOriginalFilename()
                                                .lastIndexOf(".")
                                )
                )
                .toString();
    }
}
