package com.tianji.api.client.remark;

import com.tianji.api.client.remark.fallback.RemarkClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Set;

/**
 * @author whw
 * @title: RemarkClient
 * @projectName tjxt
 * @description: TODO
 * @date 2024/3/27 16:01
 */

@FeignClient(value = "remark-service", fallbackFactory = RemarkClientFallback.class)
public interface RemarkClient {
    @GetMapping("/likes/list")
    Set<Long> isBizLiked(@RequestParam("bizIds") Iterable<Long> bizIds);
}
