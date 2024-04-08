package com.tianji.learning.service;

import com.tianji.learning.domain.vo.SignResultVO;

/**
 * @author whw
 * @title: ISignRecordService
 * @projectName tjxt
 * @description: TODO
 * @date 2024/4/7 20:23
 */
public interface ISignRecordService {
    SignResultVO addSignRecords();

    Byte[] querySignRecords();
}
