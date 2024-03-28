package com.tianji.api.client.remark.fallback;

import com.tianji.api.client.remark.RemarkClient;
import com.tianji.common.utils.CollUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.Set;

/**
 * @author whw
 * @title: RemarkClientFallback
 * @projectName tjxt
 * @description: TODO
 * @date 2024/3/27 16:03
 */
@Slf4j
public class RemarkClientFallback implements FallbackFactory<RemarkClient> {

    @Override
    public RemarkClient create(Throwable cause) {
        log.error("查询remark-service服务异常", cause);
        return new RemarkClient() {

            @Override
            public Set<Long> isBizLiked(Iterable<Long> bizIds) {
                return CollUtils.emptySet();
            }
        };
    }
}
