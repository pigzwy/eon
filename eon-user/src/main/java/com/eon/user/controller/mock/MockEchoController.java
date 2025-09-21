package com.eon.user.controller.mock;

import com.eon.user.remote.feign.dto.EchoPayload;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 提供给 Feign 示例使用的本地回声接口，返回请求头与消息内容。
 */
@RestController
@RequestMapping("/internal/mock")
@ConditionalOnProperty(prefix = "eon.demo", name = "mock-enabled", havingValue = "true", matchIfMissing = true)
public class MockEchoController {

    @GetMapping("/echo")
    public EchoPayload echo(@RequestParam("value") String value, HttpServletRequest request) {
        Map<String, String> headers = Collections.list(request.getHeaderNames()).stream()
                .filter(name -> name.startsWith("X-") || "authorization".equalsIgnoreCase(name))
                .collect(Collectors.toMap(
                        name -> name,
                        name -> Collections.list(request.getHeaders(name)).stream().collect(Collectors.joining(",")),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
        return new EchoPayload(value, headers);
    }
}
