package com.dianshang.admin.system.controller;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.system.dto.UploadResultVO;
import com.dianshang.admin.system.service.FileUploadService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class FileUploadController {

    private final FileUploadService fileUploadService;

    public FileUploadController(FileUploadService fileUploadService) {
        this.fileUploadService = fileUploadService;
    }

    @PostMapping("/image")
    public ApiResponse<UploadResultVO> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "ops") String biz) {
        String url = fileUploadService.storeImage(file, biz);
        return ApiResponse.ok(new UploadResultVO(url));
    }
}
