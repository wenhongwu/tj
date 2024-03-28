package com.tianji.remark.task;

import com.tianji.remark.domain.po.LikedStat;
import com.tianji.remark.service.ILikedRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author whw
 * @title: LikedTimesCheckTask
 * @projectName tjxt
 * @description: TODO
 * @date 2024/3/28 20:12
 */
@Component
@RequiredArgsConstructor
public class LikedTimesCheckTask {

    private static final List<String> BIZ_TYPES = List.of("QA", "NOTE");
    private static final int MAX_BIZ_SIZE = 30;

    private final ILikedRecordService recordService;

    @Scheduled(fixedDelay = 20000)
    public void checkLikedTimes() {
        for (String bizType : BIZ_TYPES) {
            recordService.readLikedTimesAndSendMessage(bizType, MAX_BIZ_SIZE);
        }
    }

}
