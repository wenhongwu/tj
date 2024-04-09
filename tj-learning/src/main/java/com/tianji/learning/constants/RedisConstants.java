package com.tianji.learning.constants;

/**
 * @author whw
 * @title: RedisConstants
 * @projectName tjxt
 * @description: TODO
 * @date 2024/4/7 20:16
 */
public interface RedisConstants {
    /**
     * 签到记录的Key的前缀： sign:uid:110:2-2301
     */
    String SIGN_RECORD_KEY_PREFIX = "sign:uid";

    /**
     * 积分排行榜的key的前缀：boards:202401
     */
    String POINTS_BOARD_KEY_PREFIX = "board:";
}
