package com.dianshang.admin.profile;

import com.dianshang.admin.common.ApiResponse;
import com.dianshang.admin.profile.dto.ProfileUpdateRequest;
import com.dianshang.admin.profile.dto.ProfileVO;
import com.dianshang.admin.profile.dto.UpdatePasswordRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/system/user/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ApiResponse<ProfileVO> getProfile() {
        return ApiResponse.ok(profileService.getCurrentProfile());
    }

    @PutMapping
    public ApiResponse<ProfileVO> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.ok(profileService.updateProfile(request));
    }

    @PutMapping("/updatePwd")
    public ApiResponse<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        profileService.updatePassword(request);
        return ApiResponse.ok(null);
    }
}
