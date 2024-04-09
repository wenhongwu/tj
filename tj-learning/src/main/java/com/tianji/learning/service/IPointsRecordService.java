package com.tianji.learning.service;

import com.tianji.learning.domain.po.PointsRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianji.learning.domain.vo.PointsStatisticsVO;
import com.tianji.learning.enums.PointsRecordType;

import java.util.List;

/**
 * <p>
 * 学习积分记录，每个月底清零 服务类
 * </p>
 *
 * @author whw
 * @since 2024-04-07
 */
public interface IPointsRecordService extends IService<PointsRecord> {

    void addPointRecord(Long userId, Integer points, PointsRecordType sign);

    List<PointsStatisticsVO> queryMyPointsToday();

    void createPointsRecordTableBySeason(Integer season);

    List<PointsRecord> queryCurrentRecordList(Integer pageSize, Integer pageNo, Integer season);
}
