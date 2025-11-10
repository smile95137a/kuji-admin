package com.group.admin.aop;

import com.group.admin.result.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class GlobalResponseAspect {

    // 攔截所有 controller public 方法
    @Pointcut("execution(public * com.group..controller..*(..))")
    public void controllerMethods() {}

    @Around("controllerMethods()")
    public Object wrapResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result;

        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            log.error(" Controller 執行異常: {} - {}", joinPoint.getSignature(), e.getMessage(), e);
            throw e; // 讓 GlobalExceptionHandler 處理
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info(" 執行: {} 完成 ({} ms)", joinPoint.getSignature(), duration);

        // ---------------------------
        // 已是 ResponseEntity<ApiResponse>
        // ---------------------------
        if (result instanceof ResponseEntity<?>) {
            Object body = ((ResponseEntity<?>) result).getBody();

            // 若裡面已是 ApiResponse，直接返回
            if (body instanceof ApiResponse<?>) {
                return result;
            }

            // 否則包成 ApiResponse.success()
            return ResponseEntity.ok(ApiResponse.success(body));
        }

        // ---------------------------
        // 🧩 2️⃣ 已是 ApiResponse
        // ---------------------------
        if (result instanceof ApiResponse<?>) {
            return ResponseEntity.ok(result);
        }

        // ---------------------------
        // 🧩 3️⃣ 其他物件
        // ---------------------------
        if (result == null) {
            return ResponseEntity.ok(ApiResponse.success(null));
        }

        // ---------------------------
        // 🧩 4️⃣ 自動包成 ApiResponse.success()
        // ---------------------------
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
