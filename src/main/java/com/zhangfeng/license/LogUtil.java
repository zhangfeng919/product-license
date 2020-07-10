package com.zhangfeng.license;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @ClassName LogUtil
 * @Description //TODO
 * @Author zhangfeng
 * @Date 2020/7/9 15:01
 * @Version 1.0
 **/
public class LogUtil {

    static Logger log = LoggerFactory.getLogger(LogUtil.class);

    static void info(String message){
        log.info(message);
    }

}
