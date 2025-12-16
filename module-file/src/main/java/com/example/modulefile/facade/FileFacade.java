package com.example.modulefile.facade;

import com.example.modulefile.service.FileDomainService;
import com.example.modulefile.service.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileFacade {

    private final FileService fileService;

    private final FileDomainService fileDomainService;

    public String imageInsert(MultipartFile image) throws Exception {
        String saveName = fileDomainService.setImageSaveName(image);
        fileService.imageInsert(image, saveName);

        return saveName;
    }

    public void deleteImage(String saveName) {
        fileService.deleteImage(saveName);
    }
}
