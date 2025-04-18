package org.example.expert.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.expert.domain.auth.exception.ForbiddenException;
import org.example.expert.domain.auth.exception.UnauthorizedException;
import org.example.expert.domain.user.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // JWT 토큰에서 사용자 정보 가져오기
        Long userId = (Long) request.getAttribute("userId");
        String userRole = (String) request.getAttribute("userRole");

        if (userId == null) {
            throw new UnauthorizedException("로그인 필요");
        }

        if (!UserRole.ADMIN.name().equals(userRole)) {
            throw new ForbiddenException("어드민만 접근 가능합니다.");
        }

        Logger logger = LoggerFactory.getLogger(AdminInterceptor.class);
        logger.info("[Admin API 접근] 사용자 ID : {}, 시간 : {}, URL : {}",
                userId,
                LocalDateTime.now(),
                request.getRequestURI());

        return true;
    }

}
