package com.eon.user.controller;

import com.eon.user.remote.feign.DemoEchoClient;
import com.eon.user.remote.feign.dto.EchoPayload;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 展示如何在用户服务中调用 Feign 客户端的示例接口。
 */
@RestController
@RequestMapping("/users/demo/feign")
@RequiredArgsConstructor
public class FeignDemoController {

    private final DemoEchoClient demoEchoClient;

    @GetMapping("/echo")
    public Map<String, Object> echo(@RequestParam(value = "message", defaultValue = "hello") String message) {
        EchoPayload payload = demoEchoClient.echo(message);
        return Map.of(
                "requestMessage", message,
                "remoteValue", payload.value(),
                "remoteHeaders", payload.headers(),
                "timestamp", Instant.now().toString()
        );
    }
}
