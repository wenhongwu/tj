package com.tianji.learning.config;

import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.tianji.learning.utils.TableInfoContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author whw
 * @title: MybatisConfiguration
 * @projectName tjxt
 * @description: TODO
 * @date 2024/4/9 13:39
 */

@Configuration
public class MybatisConfiguration {

    @Bean
    public DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor() {
        // 准备一个Map，用于存储TableNameHandler
        Map<String, TableNameHandler> map = new HashMap<>();
        map.put("points_board", (new TableNameHandler() {
            @Override
            public String dynamicTableName(String sql, String tableName) {
                return TableInfoContext.getInfo() == null ? tableName : TableInfoContext.getInfo();
            }
        }));
        return new DynamicTableNameInnerInterceptor(map);
    }
}
