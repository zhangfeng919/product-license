package com.zhangfeng.license;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
/**
 * @ClassName LicenseUtil.java
 * @Description 授权工具类
 * @Author zhangfeng
 * @Date 2020/7/9 13:58
 * @Version 1.0
 **/
public class LicenseUtil {

    static Logger logger = LoggerFactory.getLogger(LicenseUtil.class);


    /**
     * 检测授权码，设置系统变量
     * @param clientLisenceSignStr
     */
    public static void check(String clientLisenceSignStr){

        logger.info("自检开始");
        try {
            //获取服务器识别码，没有则生成
            //识别码根据服务器信息生成 aes加密

            //获取授权码

            //无授权码
            if(clientLisenceSignStr == null){
                System.setProperty(SerialNumberUtil.ALLOWFLAG, String.valueOf(AllowFlag.unauthorized.getCode()));
                logger.info(String.format("没有授权码。检测结果 %s",System.getProperty(SerialNumberUtil.ALLOWFLAG)));
                return;
            }


            //有授权码 解析授权码
            LicenseSign licenseSign = LicenseSign.init(clientLisenceSignStr);
            if(licenseSign != null){

                logger.info("lisence校验开始：");

                //获取授权码，有则拆分授权码（识别码+校验码） 无则设置标识false
                logger.info("软件授权码校验开始：");

                //根据授权码 rsa加密与校验码对比
                if(!RSAUtil.verify(licenseSign.getAesAuth(), licenseSign.getAesAuthCheckCode())){
                    logger.error("授权码校验失败！");
                    System.setProperty(SerialNumberUtil.ALLOWFLAG, String.valueOf(AllowFlag.unauthorized.getCode()));
                    logger.info(String.format("授权校验码验证失败。检测结果 %s",System.getProperty(SerialNumberUtil.ALLOWFLAG)));
                    return;
                }



                logger.info("软件授权码校验结束：");


                //根据识别码得到信息

                Map<String,Object> infoMap = licenseSign.serverinfo();

                Map<String,String> infoMapLocal = SerialNumberUtil.getAllSn();
                logger.info("服务器信息比对开始：");

                //授权码是否在有效期
                LocalDateTime now = LocalDateTime.now();
                now.plusDays(SerialNumberUtil.VALIDATEDAY);

                LocalDateTime validateTime = Instant.ofEpochMilli((Long)infoMap.get(SerialNumberUtil.REGISTERDATE)).atZone(ZoneOffset.ofHours(8)).toLocalDateTime();
                validateTime = validateTime.plusDays(SerialNumberUtil.VALIDATEDAY);

                if(now.isAfter(validateTime)){
                    System.setProperty(SerialNumberUtil.ALLOWFLAG, String.valueOf(AllowFlag.unauthorized.getCode()));
                    logger.info(String.format("授权码不在激活有效期内。检测结果 %s",System.getProperty(SerialNumberUtil.ALLOWFLAG)));
                    return;
                }



                //对比 cpuid ip mac mainboard
                for (String proper: SerialNumberUtil.INFOPROPERS) {
                    if(!infoMap.get(proper).equals(infoMapLocal.get(proper))){
                        System.setProperty(SerialNumberUtil.ALLOWFLAG, String.valueOf(AllowFlag.unauthorized.getCode()));
                        logger.info(String.format("服务器信息比对失败。检测结果 %s",System.getProperty(SerialNumberUtil.ALLOWFLAG)));
                        return;
                    }
                }

                logger.info("服务器信息比对结束：");

                logger.info("试用时间比对开始：");

                //对比 endDate
                LocalDate endDate = Instant.ofEpochMilli((Long)infoMap.get(SerialNumberUtil.ENDDATE)).atZone(ZoneOffset.ofHours(8)).toLocalDate();


                //与当前服务器时间比对
                if(endDate.isBefore(LocalDate.now())){
                    System.setProperty(SerialNumberUtil.ALLOWFLAG, String.valueOf(AllowFlag.expired.getCode()));
                    return;
                }

                logger.info("试用时间比对结束：");

                logger.info("lisence校验结束：");

                System.setProperty(SerialNumberUtil.ALLOWFLAG, String.valueOf(AllowFlag.authorized.getCode()));


                logger.info(String.format("检测结果 %s",System.getProperty(SerialNumberUtil.ALLOWFLAG)));
            }else{
                System.setProperty(SerialNumberUtil.ALLOWFLAG, String.valueOf(AllowFlag.unauthorized.getCode()));
            }
        }catch (Exception e){
            //防止定时任务挂掉
            e.printStackTrace();
            System.setProperty(SerialNumberUtil.ALLOWFLAG, String.valueOf(AllowFlag.unauthorized.getCode()));
            logger.info(String.format("检测出现错误。检测结果 %s",System.getProperty(SerialNumberUtil.ALLOWFLAG)));
        }
    }

    /**
     * 比较 授权版本与产品版本
     *
     * 只比较大版本
     * @param version 产品版本
     * @param authVersion 授权版本
     * @return
     */
    public static boolean checkVersion(String version,String authVersion){

        if(StringUtils.isNotEmpty(version) && StringUtils.isNotEmpty(authVersion) &&
         version.startsWith(authVersion+".")){
            return true;
        }

        return false;
    }

    public static String  createClientCode(){

        Map<String,String> infoMapLocal = SerialNumberUtil.getAllSn();


        logger.info("系统信息："+JSONObject.toJSONString(infoMapLocal));
        String infoStr = SignUtil.toSignString(infoMapLocal);

        return Base64.getEncoder().encodeToString(infoStr.getBytes());

    }



    public static Map<String, Object> decodeAuthCode(String clientLisenceSignStr){
        LicenseSign licenseSign = LicenseSign.init(clientLisenceSignStr);
        if(licenseSign == null){
            return Collections.EMPTY_MAP;
        }

        if(!RSAUtil.verify(licenseSign.getAesAuth(), licenseSign.getAesAuthCheckCode())){
            return Collections.EMPTY_MAP;
        }

        return licenseSign.serverinfo();
    }


    /**
     * @Author zhangfeng
     * @Description 根据字符串生成二维码图片，并将图片转为base64字符串
     * @Date 14:04 2020/7/9
     * @Param [text 待转字符串]
     * @return java.lang.String
     */
    public static String createQRCode(String text){

        BufferedImage bufferedImage = QRCodeUtil.encodeQRCode(text);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try {
            ImageIO.write(bufferedImage, "png", stream);
            return Base64.getEncoder().encodeToString(stream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
