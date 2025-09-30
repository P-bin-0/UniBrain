package com.bin.filter;

import com.bin.Exception.InValidTokenException;
import com.bin.Exception.NonTokenException;
import com.bin.Exception.UserNonLoginException;
import com.bin.entity.MyUserDetails;
import com.bin.util.JWTUtil;
import com.bin.util.RedisKeyUtil;
import com.bin.util.RedisUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().contains("/login")) {
            filterChain.doFilter(request, response);
            return;
        }
        if (request.getRequestURI().contains("/register")) {
            filterChain.doFilter(request, response);
            return;
        }
        String token = getJwtFromRequest(request);
        if (Objects.isNull(token) || token.trim().isEmpty()) {
            throw new NonTokenException("token为空");
        }
        Long userId = null;
        try {
            Claims claims = jwtUtil.getClaimsFromToken(token);
            userId = claims.get("id", Long.class);
        } catch (Exception e) {
            throw new InValidTokenException("token解析失败");
        }
        String key = RedisKeyUtil.keyLoginUser(userId);
        if (!redisUtil.hasKey(key)) {
            throw new UserNonLoginException("登录过期，请重新登录");
        }
        MyUserDetails userDetails = redisUtil.getCacheObject(key, MyUserDetails.class);
        if (Objects.isNull(userDetails)) {
            throw new UserNonLoginException("用户信息已过期，请重新登录");
        }
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }
    /**
     * 从请求头中获取JWT令牌
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")){
            return bearerToken.substring(7); // 去掉"Bearer "前缀
        }
        return null;
    }
}
