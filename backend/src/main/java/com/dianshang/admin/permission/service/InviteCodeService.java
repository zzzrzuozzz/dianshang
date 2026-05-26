package com.dianshang.admin.permission.service;

import com.dianshang.admin.common.BusinessException;
import com.dianshang.admin.common.PageResult;
import com.dianshang.admin.permission.dto.InviteCodeGenerateRequest;
import com.dianshang.admin.permission.dto.InviteCodePageRequest;
import com.dianshang.admin.permission.dto.InviteCodeVO;
import com.dianshang.admin.permission.entity.SysAdminRole;
import com.dianshang.admin.permission.entity.SysInviteCode;
import com.dianshang.admin.permission.entity.SysRole;
import com.dianshang.admin.permission.repository.SysAdminRoleRepository;
import com.dianshang.admin.permission.repository.SysInviteCodeRepository;
import com.dianshang.admin.permission.repository.SysRoleRepository;
import com.dianshang.admin.permission.support.InviteCodeSpecifications;
import com.dianshang.admin.user.AdminUser;
import com.dianshang.admin.user.AdminUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InviteCodeService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final SysInviteCodeRepository inviteCodeRepository;
    private final SysRoleRepository roleRepository;
    private final AdminUserRepository adminUserRepository;
    private final SysAdminRoleRepository adminRoleRepository;

    public InviteCodeService(SysInviteCodeRepository inviteCodeRepository,
                               SysRoleRepository roleRepository,
                               AdminUserRepository adminUserRepository,
                               SysAdminRoleRepository adminRoleRepository) {
        this.inviteCodeRepository = inviteCodeRepository;
        this.roleRepository = roleRepository;
        this.adminUserRepository = adminUserRepository;
        this.adminRoleRepository = adminRoleRepository;
    }

    @Transactional
    public List<String> generate(InviteCodeGenerateRequest req, Long operatorId) {
        SysRole role = roleRepository.findById(req.getRoleId())
                .orElseThrow(() -> new BusinessException("绑定角色不存在"));
        if (role.getStatus() != null && role.getStatus() == 0) {
            throw new BusinessException("所选角色已禁用，请更换角色");
        }
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < req.getCount(); i++) {
            String code = createUniqueCode();
            SysInviteCode entity = new SysInviteCode();
            entity.setCode(code);
            entity.setRoleId(role.getId());
            entity.setUsed(0);
            entity.setCreatedBy(operatorId);
            entity.setRemark(req.getRemark());
            entity.setCreateTime(LocalDateTime.now());
            inviteCodeRepository.save(entity);
            codes.add(code);
        }
        return codes;
    }

    public PageResult<InviteCodeVO> page(InviteCodePageRequest req) {
        Page<SysInviteCode> page = inviteCodeRepository.findAll(
                InviteCodeSpecifications.forPage(req.getUsed()),
                PageRequest.of(Math.max(req.getPage() - 1, 0), req.getPageSize(),
                        Sort.by(Sort.Direction.DESC, "createTime")));
        Map<Long, SysRole> roleMap = roleRepository.findAll().stream()
                .collect(Collectors.toMap(SysRole::getId, r -> r));
        Map<Long, AdminUser> userMap = adminUserRepository.findAll().stream()
                .collect(Collectors.toMap(AdminUser::getId, u -> u));
        List<InviteCodeVO> list = page.getContent().stream()
                .map(item -> toVO(item, roleMap, userMap))
                .toList();
        return new PageResult<>(list, page.getTotalElements(), req.getPage(), req.getPageSize());
    }

    @Transactional
    public SysInviteCode consumeForRegister(String inviteCode, String accountType, String account, Long adminId) {
        SysInviteCode code = inviteCodeRepository.findByCode(normalizeCode(inviteCode))
                .orElseThrow(() -> new BusinessException("邀请码无效"));
        if (code.getUsed() != null && code.getUsed() == 1) {
            throw new BusinessException("邀请码已被使用");
        }
        SysRole role = roleRepository.findById(code.getRoleId())
                .orElseThrow(() -> new BusinessException("邀请码绑定角色不存在"));
        if (role.getStatus() != null && role.getStatus() == 0) {
            throw new BusinessException("邀请码绑定角色已禁用");
        }
        code.setUsed(1);
        code.setUsedByAdminId(adminId);
        code.setUsedAccount(account.trim());
        code.setUsedAccountType(accountType);
        code.setUsedTime(LocalDateTime.now());
        inviteCodeRepository.save(code);

        SysAdminRole link = new SysAdminRole();
        link.setAdminId(adminId);
        link.setRoleId(role.getId());
        adminRoleRepository.save(link);
        return code;
    }

    public void verifyForReset(String inviteCode, String accountType, String account) {
        SysInviteCode code = inviteCodeRepository.findByCode(normalizeCode(inviteCode))
                .orElseThrow(() -> new BusinessException("邀请码无效"));
        if (code.getUsed() == null || code.getUsed() != 1) {
            throw new BusinessException("邀请码尚未被使用，无法用于找回密码");
        }
        if (!accountType.equalsIgnoreCase(code.getUsedAccountType())) {
            throw new BusinessException("验证方式与注册时不一致");
        }
        if (!account.trim().equalsIgnoreCase(code.getUsedAccount())) {
            throw new BusinessException("手机号/邮箱与注册时不一致");
        }
        if (code.getUsedByAdminId() == null) {
            throw new BusinessException("邀请码使用记录异常，请联系管理员");
        }
    }

    public Long adminIdForReset(String inviteCode, String accountType, String account) {
        verifyForReset(inviteCode, accountType, account);
        return inviteCodeRepository.findByCode(normalizeCode(inviteCode))
                .map(SysInviteCode::getUsedByAdminId)
                .orElseThrow(() -> new BusinessException("邀请码无效"));
    }

    private String createUniqueCode() {
        for (int attempt = 0; attempt < 20; attempt++) {
            String code = randomCode(12);
            if (!inviteCodeRepository.existsByCode(code)) {
                return code;
            }
        }
        throw new BusinessException("邀请码生成失败，请重试");
    }

    private String randomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    private String normalizeCode(String code) {
        return code != null ? code.trim().toUpperCase() : "";
    }

    private InviteCodeVO toVO(SysInviteCode item, Map<Long, SysRole> roleMap, Map<Long, AdminUser> userMap) {
        InviteCodeVO vo = new InviteCodeVO();
        vo.setId(item.getId());
        vo.setCode(item.getCode());
        vo.setRoleId(item.getRoleId());
        SysRole role = roleMap.get(item.getRoleId());
        vo.setRoleName(role != null ? role.getRoleName() : "-");
        vo.setUsed(item.getUsed());
        vo.setUsedLabel(item.getUsed() != null && item.getUsed() == 1 ? "已使用" : "未使用");
        vo.setUsedByAdminId(item.getUsedByAdminId());
        if (item.getUsedByAdminId() != null) {
            AdminUser user = userMap.get(item.getUsedByAdminId());
            vo.setUsedByUsername(user != null ? user.getUsername() : "-");
        }
        vo.setUsedAccount(item.getUsedAccount());
        vo.setUsedAccountType(item.getUsedAccountType());
        vo.setUsedTime(item.getUsedTime() != null ? item.getUsedTime().format(DT_FMT) : null);
        vo.setCreateTime(item.getCreateTime() != null ? item.getCreateTime().format(DT_FMT) : null);
        vo.setRemark(item.getRemark());
        return vo;
    }
}
