package com.eon.user.remote.feign.dto;

import java.util.Map;

/**
 * 用于演示 Feign 返回的回显数据。
 */
public record EchoPayload(String value, Map<String, String> headers) {
}
