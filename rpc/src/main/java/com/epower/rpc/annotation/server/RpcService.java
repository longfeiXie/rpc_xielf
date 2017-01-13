package com.epower.rpc.annotation.server;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * RpcServer 注解
 * 
 * @author xielf
 *
 */
@Documented
@Target({ ElementType.TYPE }) // 注解用处
@Retention(RetentionPolicy.RUNTIME) // 保留时间
@Component
public @interface RpcService {

	String value() default "";
}
