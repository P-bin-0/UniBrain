package com.bin.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.bin.Exception.InValidTokenException;
import com.bin.Exception.NonTokenException;
import com.bin.Exception.UserNonLoginException;
import com.bin.entity.MyUserDetails;
import com.bin.response.ApiResponse;
import com.bin.util.JWTUtil;
import com.bin.util.RedisKeyUtil;
import com.bin.util.RedisUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
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
import java.io.PrintWriter;
import java.util.Objects;

@Component
public class JWTAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private ObjectMapper objectMapper; // 用于将对象转为JSON

    /*@Override
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
    }*/
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().contains("/login") || request.getRequestURI().contains("/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = getJwtFromRequest(request);
            if (Objects.isNull(token) || token.trim().isEmpty()) {
                throw new NonTokenException("token为空，请重新登录");
            }

            Long userId = null;
            try {
                Claims claims = jwtUtil.getClaimsFromToken(token);
                userId = claims.get("id", Long.class);
            } catch (ExpiredJwtException e) {
                // 单独捕获Token过期异常，明确提示登录过期
                throw new UserNonLoginException("登录已过期，请重新登录");
            } catch (Exception e) {
                // 其他解析错误（如签名错误、格式错误）才提示解析失败
                throw new InValidTokenException("token无效或已被篡改");
            }

            String key = RedisKeyUtil.keyLoginUser(userId);
            if (!redisUtil.hasKey(key)) {
                throw new UserNonLoginException("登录状态已失效，请重新登录");
            }

            MyUserDetails userDetails = redisUtil.getCacheObject(key, MyUserDetails.class);
            if (Objects.isNull(userDetails)) {
                throw new UserNonLoginException("用户信息已过期，请重新登录");
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            ApiResponse<Void> errorResponse;
            if (e instanceof NonTokenException) {
                errorResponse = ApiResponse.error(401, e.getMessage());
            } else if (e instanceof InValidTokenException) {
                errorResponse = ApiResponse.error(401, e.getMessage());
            } else if (e instanceof UserNonLoginException) {
                errorResponse = ApiResponse.error(401, e.getMessage());
            } else {
                errorResponse = ApiResponse.error(500, "服务器内部错误");
            }

            PrintWriter out = response.getWriter();
            out.write(objectMapper.writeValueAsString(errorResponse));
            out.flush();
            out.close();
        }
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
