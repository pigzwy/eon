package com.eon.user.remote.feign;

import com.eon.user.remote.feign.dto.EchoPayload;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 简单的 Feign 客户端示例，默认指向本地的模拟回声接口。
 */
@FeignClient(
        name = "eon-user-echo",
        contextId = "demoEchoClient",
        url = "${eon.demo.echo-url:http://localhost:4000}",
        path = "/internal/mock"
)
public interface DemoEchoClient {

    /**
     * 将消息透传给远端回声接口，返回回显数据与请求头。
     */
    @GetMapping("/echo")
    EchoPayload echo(@RequestParam("value") String value);
}
