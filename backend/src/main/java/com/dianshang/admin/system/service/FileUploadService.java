package com.dianshang.admin.system.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.config.UploadProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/jpg");
    private static final Set<String> EXT = Set.of(".jpg", ".jpeg", ".png");

    private final UploadProperties properties;

    public FileUploadService(UploadProperties properties) {
        this.properties = properties;
    }

    public String storeImage(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的图片");
        }
        long maxBytes = properties.getMaxSizeMb() * 1024 * 1024;
        if (file.getSize() > maxBytes) {
            throw new BusinessException("图片大小不能超过 " + properties.getMaxSizeMb() + "MB");
        }
        String contentType = file.getContentType();
        String original = file.getOriginalFilename();
        String ext = resolveExtension(original, contentType);
        if (!EXT.contains(ext)) {
            throw new BusinessException("仅支持 JPG、PNG 格式");
        }
        if (contentType != null && !ALLOWED.contains(contentType.toLowerCase())) {
            throw new BusinessException("仅支持 JPG、PNG 格式");
        }

        String safeSub = StringUtils.hasText(subDir) ? subDir.replaceAll("[^a-zA-Z0-9_-]", "") : "common";
        Path dir = Path.of(properties.getDir()).toAbsolutePath().normalize().resolve(safeSub);
        try {
            Files.createDirectories(dir);
            String filename = UUID.randomUUID().toString().replace("-", "") + ext;
            Path target = dir.resolve(filename);
            file.transferTo(target);
            return "/uploads/" + safeSub + "/" + filename;
        } catch (IOException e) {
            throw new BusinessException("图片上传失败");
        }
    }

    private String resolveExtension(String original, String contentType) {
        if (StringUtils.hasText(original)) {
            String lower = original.toLowerCase();
            if (lower.endsWith(".jpeg")) {
                return ".jpeg";
            }
            if (lower.endsWith(".jpg")) {
                return ".jpg";
            }
            if (lower.endsWith(".png")) {
                return ".png";
            }
        }
        if ("image/png".equalsIgnoreCase(contentType)) {
            return ".png";
        }
        return ".jpg";
    }
}
