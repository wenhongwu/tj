package com.tianji.promotion.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianji.api.cache.CategoryCache;
import com.tianji.common.domain.dto.PageDTO;
import com.tianji.common.exceptions.BadRequestException;
import com.tianji.common.exceptions.BizIllegalException;
import com.tianji.common.utils.BeanUtils;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.DateUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.promotion.constants.PromotionConstants;
import com.tianji.promotion.domain.dto.CouponFormDTO;
import com.tianji.promotion.domain.dto.CouponIssueFormDTO;
import com.tianji.promotion.domain.po.Coupon;
import com.tianji.promotion.domain.po.CouponScope;
import com.tianji.promotion.domain.query.CouponQuery;
import com.tianji.promotion.domain.vo.CouponDetailVO;
import com.tianji.promotion.domain.vo.CouponPageVO;
import com.tianji.promotion.domain.vo.CouponScopeVO;
import com.tianji.promotion.enums.CouponStatus;
import com.tianji.promotion.enums.ObtainType;
import com.tianji.promotion.mapper.CouponMapper;
import com.tianji.promotion.service.ICouponScopeService;
import com.tianji.promotion.service.ICouponService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.promotion.service.IExchangeCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.Future;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.tianji.promotion.enums.CouponStatus.*;

/**
 * <p>
 * 优惠券的规则信息 服务实现类
 * </p>
 *
 * @author whw
 * @since 2024-04-10
 */
@Service
@RequiredArgsConstructor
public class CouponServiceImpl extends ServiceImpl<CouponMapper, Coupon> implements ICouponService {

    private final ICouponScopeService scopeService;
    private final IExchangeCodeService codeService;
    private final CategoryCache categoryCache;
    private final StringRedisTemplate redisTemplate;


    @Override
    @Transactional
    public void saveCoupon(CouponFormDTO dto) {
        // 1.保存优惠券
        // 1.1转PO
        Coupon coupon = BeanUtils.copyBean(dto, Coupon.class);
        // 1.2.保存
        save(coupon);

        if (!dto.getSpecific()) {
            // 没有范围限定
            return;
        }
        Long couponId = coupon.getId();
        // 2.保存限定范围
        List<Long> scopes = dto.getScopes();
        if (CollUtils.isEmpty(scopes)) {
            throw new BadRequestException("限定范围不能为空");
        }
        // 2.1.转换PO
        List<CouponScope> list = scopes.stream()
                .map(bizId -> new CouponScope().setBizId(bizId).setCouponId(couponId))
                .collect(Collectors.toList());
        // 2.2.保存
        scopeService.saveBatch(list);
    }

    @Override
    public PageDTO<CouponPageVO> queryCouponByPage(CouponQuery query) {
        Integer status = query.getStatus();
        String name = query.getName();
        Integer type = query.getType();
        // 1.分页查询
        Page<Coupon> page = lambdaQuery()
                .eq(type != null, Coupon::getType, type)
                .eq(status != null, Coupon::getStatus, status)
                .like(StringUtils.isNotBlank(name), Coupon::getName, name)
                .page(query.toMpPageDefaultSortByCreateTimeDesc());
        // 2.处理VO
        List<Coupon> records = page.getRecords();
        if (CollUtils.isEmpty(records)) {
            return PageDTO.empty(page);
        }
        List<CouponPageVO> list = BeanUtils.copyList(records, CouponPageVO.class);
        // 3.返回
        return PageDTO.of(page, list);
    }

    @Override
    @Transactional
    public void beginIssue(CouponIssueFormDTO dto) {
        // 1.查询优惠券
        Coupon coupon = getById(dto.getId());
        if (coupon == null) {
            throw new BadRequestException("优惠券不存在！");
        }
        // 2.判断优惠券状态，是否是暂停or等待发放
        if (coupon.getStatus() != CouponStatus.DRAFT && coupon.getStatus() != PAUSE) {
            throw new BizIllegalException("优惠券状态错误！");
        }
        // 3.判断是否是立即发放
        LocalDateTime issueBeginTime = dto.getIssueBeginTime();
        LocalDateTime now = LocalDateTime.now();
        boolean isBegin = issueBeginTime == null || !issueBeginTime.isAfter(now);
        // 4.更新优惠券
        // 4.1.拷贝属性到PO
        Coupon c = BeanUtils.copyBean(dto, Coupon.class);
        // 4.2.更新状态
        if (isBegin) {
            c.setStatus(ISSUING);
            c.setIssueBeginTime(now);
        } else
            c.setStatus(UN_ISSUE);
        // 4.3.写入数据库
        updateById(c);

        // 5.添加缓存
        if (isBegin) {
            coupon.setIssueBeginTime(c.getIssueBeginTime());
            coupon.setIssueEndTime(c.getIssueEndTime());
            cacheCouponInfo(coupon);
        }

        // 6.判断是否需要生成兑换码，优惠券类型必须是兑换码，优惠全状态必须是待发行状态
        if(coupon.getObtainWay() == ObtainType.ISSUE && coupon.getStatus() == CouponStatus.DRAFT){
            coupon.setIssueEndTime(c.getIssueEndTime());
            codeService.asyncGenerateCode(coupon);
        }
    }

    private void cacheCouponInfo(Coupon coupon) {
        // 1.组织数据
        Map<String, String> map = new HashMap<>(4);
        map.put("issueBeginTime", String.valueOf(DateUtils.toEpochMilli(coupon.getIssueBeginTime())));
        map.put("issueEndTime", String.valueOf(DateUtils.toEpochMilli(coupon.getIssueEndTime())));
        map.put("totalNum", String.valueOf(coupon.getTotalNum()));
        map.put("userLimit", String.valueOf(coupon.getUserLimit()));
        // 2.写缓存
        redisTemplate.opsForHash().putAll(PromotionConstants.COUPON_CACHE_KEY_PREFIX + coupon.getId(), map);
    }

    @Override
    public void deleteById(Long id) {
        // 1.查询
        Coupon coupon = getById(id);
        if (coupon == null || coupon.getStatus() != DRAFT) {
            throw new BadRequestException("优惠券不存在或者优惠券正在使用中");
        }
        // 2.删除优惠券
        boolean success = remove(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getId, id)
                .eq(Coupon::getStatus, DRAFT)
        );
        if (!success) {
            throw new BadRequestException("优惠券不存在或者优惠券正在使用中");
        }
        // 3.删除优惠券对应限定范围
        if(!coupon.getSpecific()){
            return;
        }
        scopeService.remove(new LambdaQueryWrapper<CouponScope>().eq(CouponScope::getCouponId, id));

    }

    @Override
    public CouponDetailVO queryCouponById(Long id) {
        // 1.查询优惠券
        Coupon coupon = getById(id);
        // 2.转换VO
        CouponDetailVO vo = BeanUtils.copyBean(coupon, CouponDetailVO.class);
        if (vo == null || !coupon.getSpecific()) {
            // 数据不存在，或者没有限定范围，直接结束
            return vo;
        }
        // 3.查询限定范围
        List<CouponScope> scopes = scopeService.lambdaQuery().eq(CouponScope::getCouponId, id).list();
        if (CollUtils.isEmpty(scopes)) {
            return vo;
        }
        List<CouponScopeVO> scopeVOS = scopes.stream()
                .map(CouponScope::getBizId)
                .map((Long cateId) -> {
                    return new CouponScopeVO(cateId, categoryCache.getNameByLv3Id(cateId));
                })
                .collect(Collectors.toList());
        vo.setScopes(scopeVOS);
        return vo;
    }

    @Override
    @Transactional
    public void pauseIssue(Long id) {
        // 1.查询旧优惠券
        Coupon coupon = getById(id);
        if (coupon == null) {
            throw new BadRequestException("优惠券不存在");
        }

        // 2.当前券状态必须是未开始或进行中
        CouponStatus status = coupon.getStatus();
        if (status != UN_ISSUE && status != ISSUING) {
            // 状态错误，直接结束
            return;
        }

        // 3.更新状态
        boolean success = lambdaUpdate()
                .set(Coupon::getStatus, PAUSE)
                .eq(Coupon::getId, id)
                .in(Coupon::getStatus, UN_ISSUE, ISSUING)
                .update();
        if (!success) {
            // 可能是重复更新，结束
            log.error("重复暂停优惠券");
        }


        // 4.删除缓存
        redisTemplate.delete(PromotionConstants.COUPON_CACHE_KEY_PREFIX + id);

    }

    @Override
    public void beginIssueBatch(List<Coupon> coupons) {
        // 1.更新券状态
        for (Coupon c : coupons) {
            c.setStatus(CouponStatus.ISSUING);
        }
        updateBatchById(coupons);
        // 2.批量缓存
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection src = (StringRedisConnection) connection;
            for (Coupon coupon : coupons) {
                // 2.1.组织数据
                Map<String, String> map = new HashMap<>(4);
                map.put("issueBeginTime", String.valueOf(DateUtils.toEpochMilli(coupon.getIssueBeginTime())));
                map.put("issueEndTime", String.valueOf(DateUtils.toEpochMilli(coupon.getIssueEndTime())));
                map.put("totalNum", String.valueOf(coupon.getTotalNum()));
                map.put("userLimit", String.valueOf(coupon.getUserLimit()));
                // 2.2.写缓存
                src.hMSet(PromotionConstants.COUPON_CACHE_KEY_PREFIX + coupon.getId(), map);
            }
            return null;
        });
    }


}
