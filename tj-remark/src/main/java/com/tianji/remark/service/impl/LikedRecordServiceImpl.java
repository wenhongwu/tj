package com.tianji.remark.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tianji.common.autoconfigure.mq.RabbitMqHelper;
import com.tianji.common.utils.CollUtils;
import com.tianji.common.utils.StringUtils;
import com.tianji.common.utils.UserContext;
import com.tianji.remark.constants.RedisConstants;
import com.tianji.remark.domain.dto.LikeRecordFormDTO;
import com.tianji.api.dto.reamark.LikedTimesDTO;
import com.tianji.remark.domain.po.LikedRecord;
import com.tianji.remark.mapper.LikedRecordMapper;
import com.tianji.remark.service.ILikedRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianji.remark.service.ILikedStatService;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.cms.CMSAuthenticatedGenerator;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.tianji.common.constants.MqConstants.Exchange.LIKE_RECORD_EXCHANGE;
import static com.tianji.common.constants.MqConstants.Key.LIKED_TIMES_KEY_TEMPLATE;

/**
 * <p>
 * 点赞记录表 服务实现类
 * </p>
 *
 * @author whw
 * @since 2024-03-23
 */
@Service
@RequiredArgsConstructor
public class LikedRecordServiceImpl extends ServiceImpl<LikedRecordMapper, LikedRecord> implements ILikedRecordService {

    private final ILikedStatService statService;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public void addLikeRecord(LikeRecordFormDTO recordDTO) {
        // 1.基于前端的参数，判断时执行点赞还是取消点赞
        boolean success = recordDTO.getLiked()? like(recordDTO) : unlike(recordDTO);
        // 2.判断是否执行成功，如果失败，则直接结束
        if (!success) {
            return;
        }
//        // 3.更新点赞数量
//        statService.updateLiedTimes(recordDTO);
        // 3.如果执行成功，统计点赞总数
        Long likedTimes = redisTemplate.opsForSet()
                .size(RedisConstants.LIKE_BIZ_KEY_PREFIX + recordDTO.getBizId());
        if (likedTimes == null) {
            return;
        }
        // 4.缓存点赞总数到Redis
        redisTemplate.opsForZSet().add(
                RedisConstants.LIKE_COUNT_KEY_PREFIX + recordDTO.getBizType(),
                recordDTO.getBizId().toString(),
                likedTimes
        );
    }

    @Override
    public Set<Long> isBizLiked(List<Long> bizIds) {
//        //1.获取登录用户id
//        Long userId = UserContext.getUser();
//        //2.查询点赞状态
//        List<LikedRecord> list = lambdaQuery()
//                .in(LikedRecord::getBizId, bizIds)
//                .eq(LikedRecord::getUserId, userId)
//                .list();
//        //3.返回结果
//        return list.stream().map(LikedRecord::getBizId).collect(Collectors.toSet());
        // 1.获取登录用户id
        Long userId = UserContext.getUser();
        // 2.查询点赞状态
        List<Object> objects = redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection src = (StringRedisConnection) connection;
            for (Long bizId : bizIds) {
                String key = RedisConstants.LIKE_BIZ_KEY_PREFIX + bizId;
                src.sIsMember(key, userId.toString());
            }
            return null;
        });
        // 3.返回结果
        return IntStream.range(0, objects.size()) // 创建从0到集合size的流
                .filter(i -> (boolean) objects.get(i)) // 遍历每个元素，保留结果为true的角标i
                .mapToObj(bizIds::get)// 用角标i取bizIds中的对应数据，就是点赞过的id
                .collect(Collectors.toSet());// 收集
    }

    @Override
    public void readLikedTimesAndSendMessage(String bizType, int maxBizSize) {
        // 1.读取并以出Redis中缓存的点赞总数
        String key = RedisConstants.LIKE_COUNT_KEY_PREFIX + bizType;
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet().popMin(key, maxBizSize);
        if (CollUtils.isEmpty(tuples)) {
            return;
        }
        // 2.数据转换
        ArrayList<LikedTimesDTO> list = new ArrayList<>(tuples.size());
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            String bizId = tuple.getValue();
            Double likedTimes = tuple.getScore();
            if (bizId == null || likedTimes == null) {
                continue;
            }
            list.add(LikedTimesDTO.of(Long.valueOf(bizId), likedTimes.intValue()));
        }
        // 3.批量修改点赞数量
        statService.updateLiedTimes(bizType, list);
    }

    private boolean unlike(LikeRecordFormDTO recordDTO) {
        return remove(new QueryWrapper<LikedRecord>()
                .lambda()
                .eq(LikedRecord::getUserId, UserContext.getUser())
                .eq(LikedRecord::getBizId, recordDTO.getBizId())
        );
    }

    private boolean like(LikeRecordFormDTO recordDTO) {
        Long userId = UserContext.getUser();
        //1.查询点赞记录
        Integer count = lambdaQuery()
                .eq(LikedRecord::getUserId, userId)
                .eq(LikedRecord::getBizId, recordDTO.getBizId())
                .count();
        //2.判断是否存在，如果已经存在，直接结束
        if (count > 0) {
            return false;
        }
        //3.如果不存在，则新增
        LikedRecord likedRecord = new LikedRecord();
        likedRecord.setBizId(recordDTO.getBizId());
        likedRecord.setUserId(userId);
        likedRecord.setBizType(recordDTO.getBizType());
        save(likedRecord);
        return true;

    }


}
