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
    private static final Set<String> VIDEO_ALLOWED = Set.of("video/mp4");

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

    public String storeVideo(MultipartFile file, String subDir) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的视频");
        }
        long maxBytes = Math.max(properties.getMaxSizeMb(), 20) * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new BusinessException("视频大小不能超过 " + Math.max(properties.getMaxSizeMb(), 20) + "MB");
        }
        String original = file.getOriginalFilename();
        String ext = ".mp4";
        if (StringUtils.hasText(original) && original.toLowerCase().endsWith(".mp4")) {
            ext = ".mp4";
        }
        String contentType = file.getContentType();
        if (contentType != null && !VIDEO_ALLOWED.contains(contentType.toLowerCase())) {
            throw new BusinessException("仅支持 MP4 格式");
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
            throw new BusinessException("视频上传失败");
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
