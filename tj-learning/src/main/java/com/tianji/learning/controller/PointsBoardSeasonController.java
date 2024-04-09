package com.tianji.learning.controller;


import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.learning.domain.po.PointsBoardSeason;
import com.tianji.learning.service.IPointsBoardSeasonService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author whw
 * @since 2024-04-07
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/boards")
@Api(tags = "积分相关接口")
public class PointsBoardSeasonController {

    private final IPointsBoardSeasonService seasonService;

    @ApiOperation("查询赛季信息列表")
    @GetMapping("/seasons/list")
    public List<PointsBoardSeason> queryPointsBoardSeasons(){
        // 1.获取时间
        LocalDateTime now = LocalDateTime.now();

        // 2.查询赛季列表，必须是当前赛季之前的（开始时间小于等于当前时间）
        List<PointsBoardSeason> list =  seasonService.lambdaQuery()
                .le(PointsBoardSeason::getBeginTime, now).list();
        if (CollUtils.isEmpty(list)) {
            return CollUtils.emptyList();
        }
        // 3.返回VO
        return BeanUtils.copyToList(list, PointsBoardSeason.class);
    }

}