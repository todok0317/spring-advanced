package org.example.expert.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.expert.domain.user.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect // AOP에서 "공통 기능"을 가진 클래스라는 의미
@Component // 스프링이 자동으로 이 클래스를 Bean으로 등록해줌
public class AdminApiLoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(AdminApiLoggingAspect.class);

    // @Around : 해당 메서드 실행 전후에 끼어들어 실행되는 AOP 어드바이스
    // exection(..) : AOP가 적용될 메서드 명시 (여기서는 deleteComment(), changeUserRole())
    // 즉, 이 두 메서드에 들어오는 요청과 나가는 응답을 로깅하겠다는 뜻
    @Around("execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..)) || " +
            "execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))")
    public Object logAdminApi(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        Long userId = (Long) request.getAttribute("userId");
        String email = (String) request.getAttribute("email");
        UserRole userRole = UserRole.of((String) request.getAttribute("userRole"));

        String url = request.getRequestURI();
        String method = request.getMethod();
        LocalDateTime requestTime = LocalDateTime.now();

        // joinPoint는 지금 AOP가 적용된 대상 메서드를 나타내는 객체
        // .getArgs()는 그 메서드에 들어온 매개변수들을 배열로 가져와
        // instanceof 연산자 : 객체가 어떤 클래스인지 어떤 클래스를 상속받았는지 확인하는데 사용하는 연산자.
        Object[] args = joinPoint.getArgs();
        String requestBody = Arrays.stream(args)
                // HttpServletRequest, HttpServletResponse 타입은 직렬화(문자열로 바꾸기) 가 안 되기 때문에, 이런 타입은 제외하고 필터링 해줌
                // 즉, 우리가 요청에서 진짜 사용자 정보(DTO 등)만 골라서 직렬화 하는 것임.
                .filter(arg -> !(arg instanceof HttpServletRequest || arg instanceof HttpServletResponse))
                // 각 파라미터 arg를 문자열(JSON) 으로 바꾸기 위해 ObjectMapper를 사용함.
                // 예 : UserDto → {"id":1, "name":"홍길동"} 이런 식으로 바뀜.
                .map(arg -> {
                    try {
                        return new ObjectMapper().writeValueAsString(arg);
                    } catch (JsonProcessingException e) {
                        return "직렬화 실패";
                    }
                })
                // JSON으로 바뀐 파라미터들을 콤마(,)로 이어 붙여서 문자열로 만드는 과정
                // 예: {"id":1}, {"name":"홍길동"}
                .collect(Collectors.joining(", "));

        logger.info("=== [Admin API 요청] ===");
        logger.info("요청자 ID: {}", userId);
        logger.info("요청 URL: {} {}", method, url);
        logger.info("요청 시각: {}", requestTime);
        logger.info("요청 본문: {}", requestBody);

        Object result = joinPoint.proceed();

        String responseBody = new ObjectMapper().writeValueAsString(result);

        logger.info("응답 본문: {}", responseBody);
        logger.info("========================");

        return result;
    }
}

