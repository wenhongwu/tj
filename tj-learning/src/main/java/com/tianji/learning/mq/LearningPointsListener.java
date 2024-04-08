package com.tianji.learning.mq;

import com.tianji.common.constants.MqConstants;
import com.tianji.learning.domain.po.PointsRecord;
import com.tianji.learning.enums.PointsRecordType;
import com.tianji.learning.mq.message.SignInMessage;
import com.tianji.learning.service.IPointsRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * @author whw
 * @title: LearningPointsListener
 * @projectName tjxt
 * @description: TODO
 * @date 2024/4/7 21:07
 */
@Component
@RequiredArgsConstructor
public class LearningPointsListener {

    private final IPointsRecordService recordService;

    //监听签到时间
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "sign.points.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.LEARNING_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.SIGN_IN
    ))
    public void listenSignInMessage(SignInMessage message) {
        recordService.addPointRecord(message.getUserId(), message.getPoints(), PointsRecordType.SIGN);
    }

    // 监听新增互动问答事件
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "qa.points.queue", durable = "true"),
            exchange = @Exchange(name = MqConstants.Exchange.LEARNING_EXCHANGE, type = ExchangeTypes.TOPIC),
            key = MqConstants.Key.WRITE_REPLY
    ))
    public void listenWriteReplyMessage(Long userId){
        recordService.addPointRecord(userId, 5, PointsRecordType.QA);
    }
}
