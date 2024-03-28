package com.tianji.remark.service;

import com.tianji.api.dto.reamark.LikedTimesDTO;
import com.tianji.remark.domain.dto.LikeRecordFormDTO;
import com.tianji.remark.domain.po.LikedStat;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 点赞统计表 服务类
 * </p>
 *
 * @author whw
 * @since 2024-03-23
 */
public interface ILikedStatService extends IService<LikedStat> {

    void updateLiedTimes(String bizType, List<LikedTimesDTO> msg);
}
