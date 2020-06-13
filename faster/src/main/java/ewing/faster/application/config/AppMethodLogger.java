package ewing.faster.application.config;

import ewing.common.utils.GsonUtils;
import io.swagger.annotations.ApiOperation;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

/**
 * 应用中业务方法调用的日志切面。
 *
 * @author Ewing
 */
@Aspect
@Configuration
@ConditionalOnProperty(name = "appMethodLogger.enable", havingValue = "true")
public class AppMethodLogger {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(AppMethodLogger.class.getSimpleName());

    @Around("@within(org.springframework.stereotype.Controller)" +
            " || @within(org.springframework.web.bind.annotation.RestController)")
    public Object logging(ProceedingJoinPoint point) throws Throwable {
        Object result;
        Signature signature = point.getSignature();

        // 此处判断可以节省准备日志内容的时间
        if (LOGGER.isInfoEnabled() && signature instanceof MethodSignature) {

            // 获取方法名、方法上的Swagger注释
            Method method = ((MethodSignature) signature).getMethod();
            String methodName = method.getDeclaringClass()
                    .getName() + "." + method.getName();

            ApiOperation operation = method.getAnnotation(ApiOperation.class);
            String methodDetail = operation == null ? methodName
                    : operation.value() + "：" + methodName;

            // 记录执行日志 方法名、参数、返回值、异常信息等
            String invokeLog = "Invoke: " + methodDetail + argsToString(point.getArgs());
            long time = System.currentTimeMillis();

            try {
                result = point.proceed();
            } catch (Throwable throwable) {
                LOGGER.info(invokeLog + " At: " + (System.currentTimeMillis() - time)
                        + "ms Throw: " + throwable.getMessage());
                // 原来的异常继续抛出去
                throw throwable;
            }

            String returnValue = method.getReturnType() == void.class ? "ms" : "ms Return: " + GsonUtils.toJson(result);
            LOGGER.info(invokeLog + " Cost: " + (System.currentTimeMillis() - time) + returnValue);

        } else {
            result = point.proceed();
        }

        return result;
    }

    private String argsToString(Object[] args) {
        if (args.length == 0) {
            return "()";
        }
        String json = GsonUtils.toJson(args);
        return '(' + json.substring(json.indexOf('[') + 1, json.lastIndexOf(']')) + ')';
    }

}