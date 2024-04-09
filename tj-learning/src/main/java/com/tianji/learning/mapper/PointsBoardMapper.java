package com.tianji.learning.mapper;

import com.tianji.learning.domain.po.PointsBoard;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 学霸天梯榜 Mapper 接口
 * </p>
 *
 * @author whw
 * @since 2024-04-07
 */
public interface PointsBoardMapper extends BaseMapper<PointsBoard> {

    void createPointsBoardTable(@Param("tableName") String tableName);
}
