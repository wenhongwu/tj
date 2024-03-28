package com.tianji.remark.mapper;

import com.tianji.remark.domain.po.LikedStat;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * <p>
 * 点赞统计表 Mapper 接口
 * </p>
 *
 * @author whw
 * @since 2024-03-23
 */
public interface LikedStatMapper extends BaseMapper<LikedStat> {

    void updateLikedTimes(Long bizId, int i);
}
