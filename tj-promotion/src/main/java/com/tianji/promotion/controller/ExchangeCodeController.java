package com.tianji.promotion.controller;


import com.tianji.common.domain.dto.PageDTO;
import com.tianji.promotion.domain.query.CouponQuery;
import com.tianji.promotion.domain.vo.CouponPageVO;
import com.tianji.promotion.service.ICouponScopeService;
import com.tianji.promotion.service.ICouponService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 兑换码 前端控制器
 * </p>
 *
 * @author whw
 * @since 2024-04-10
 */
@RestController
@RequestMapping("/codes")
@RequiredArgsConstructor
public class ExchangeCodeController {

    private final ICouponService couponService;


}
