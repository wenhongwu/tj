package com.tianji.learning.handler;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.DateUtils;
import com.tianji.learning.constants.RedisConstants;
import com.tianji.learning.domain.po.PointsBoard;
import com.tianji.learning.domain.po.PointsRecord;
import com.tianji.learning.service.IPointsBoardSeasonService;
import com.tianji.learning.service.IPointsBoardService;
import com.tianji.learning.service.IPointsRecordService;
import com.tianji.learning.utils.TableInfoContext;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

import static com.tianji.learning.constants.LearningConstant.POINTS_BOARD_TABLE_PREFIX;
import static com.tianji.learning.constants.LearningConstant.POINT_RECORD_TABLE_PREFIX;

/**
 * @author whw
 * @title: PointsRecordPersistentHandler
 * @projectName tjxt
 * @description: TODO
 * @date 2024/4/9 15:14
 */

@Component
@RequiredArgsConstructor
public class PointsRecordPersistentHandler {

    private final IPointsRecordService recordService;
    private final IPointsBoardSeasonService seasonService;

    @XxlJob("createRecordTableJob")
    public void createPointBoardTableOfLastSeason() {
        // 1.获取上月时间
        LocalDateTime time = LocalDateTime.now().minusMonths(1);
        // 2.查询赛季id
        Integer season = seasonService.querySeasonByTime(time);
        if (season == null) {
            // 赛季不存在
            return;
        }
        // 3.创建表,并复制数据到新表
        recordService.createPointsRecordTableBySeason(season);
    }

    @XxlJob("savePointsRecord2DB")
    public void savePointsBoard2DB() {
        // 1.获取上月时间
        LocalDateTime time = LocalDateTime.now().minusMinutes(1);
        // 2.计算动态表明
        // 2.1.查询赛季信息
        Integer season = seasonService.querySeasonByTime(time);
        // 3.2.查询数据
        int index = XxlJobHelper.getShardIndex();
        int total = XxlJobHelper.getShardTotal();
        int pageNo = index + 1;
        int pageSize = 10;
        while (true) {
            List<PointsRecord> list = recordService.queryCurrentRecordList(pageNo, pageSize, season);

            if (CollUtils.isEmpty(list)) {
                break;
            }
            // 4.持久化到数据库
            recordService.saveBatch(list);
            // 5.翻页
            pageNo += total;
        }
    }



    @XxlJob("clearPointsRecordFromMySQL")
    public void clearPointsBoardFromRedis() {
        // wrapper用来生成where语句，为空则全部删除
        recordService.remove(new QueryWrapper<>());
    }
}
