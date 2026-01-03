package com.lab201.monitor.common.config;

import com.lab201.monitor.common.login.LoginUser;
import com.lab201.monitor.common.login.LoginUserHolder;
import com.lab201.monitor.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthenticationInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求 (处理跨域预检)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1. 获取 Token (注意这里用的 header key 是 access-token)
        String token = request.getHeader("access-token");
        
        // 2. 解析 Token
        Claims claims = JwtUtil.parseToken(token);
        Long userId = claims.get("userId", Long.class);
        String username = claims.get("username", String.class);
        String role = claims.get("role",String.class);

        // 3. 存入ThreadLocal
        LoginUserHolder.setLoginUser(new LoginUser(userId, username, role));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 请求结束后清理线程变量，防止内存泄漏
        LoginUserHolder.clear();
    }
}