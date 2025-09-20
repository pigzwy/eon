package com.eon.common.security.context;

import java.lang.annotation.*;

/**
 * 控制器参数标记，自动注入当前用户信息。
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
