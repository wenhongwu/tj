package com.tianji.remark.constants;

/**
 * @author whw
 * @title: RedisConstants
 * @projectName tjxt
 * @description: TODO
 * @date 2024/3/28 19:53
 */
public interface RedisConstants {
    // 给业务点赞的用户集合的key前缀，后缀是业务id
    String LIKE_BIZ_KEY_PREFIX = "likes:set:biz:";
    // 给业务点赞数统计的key前缀，后缀是业务类型
    String LIKE_COUNT_KEY_PREFIX = "kikes:times:type:";
}
