package com.tianji.learning.mq.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author whw
 * @title: SignInMessage
 * @projectName tjxt
 * @description: TODO
 * @date 2024/4/7 21:02
 */
@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class SignInMessage {
    private Long userId;
    private Integer points;
}
