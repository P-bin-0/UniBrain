package com.bin.util;

import com.bin.entity.MyUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtil {
    /**
     * 获取当前登录用户的 ID
     * @return 当前登录用户的 ID，若未登录则返回 null
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof MyUserDetails userDetails) {
            return userDetails.getUser().getId();
        }
        return null;
    }
}
