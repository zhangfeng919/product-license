package com.zhangfeng.license;


import com.alibaba.fastjson.JSONObject;

import java.util.Base64;
import java.util.Map;
/**
 * @ClassName LicenseSign.java
 * @Description 授权字符串各组成部分
 * @Author zhangfeng
 * @Date 2020/7/9 13:57
 * @Version 1.0
 **/
public class LicenseSign {

    /**
     * aes密码经过rsa私钥加密后数据
     */
    private String rsaaespassword;

    /**
     * 授权码aes加密后数据
     */
    private String aesAuth;

    /**
     * 校验码rsa私钥加密
     */
    private String aesAuthCheckCode;


    private LicenseSign(){}

    /**
     * 授权码解析
     * @param licenseSignStr   rsa私钥加密aes密码 + SignUtil.SPLIT +aes加密+ SignUtil.SPLIT +校验码（aes加密的rsa签名）
     * @return
     */
    public static LicenseSign init(String licenseSignStr){
        if (licenseSignStr == null){
            return null;
        }
        licenseSignStr = new String(Base64.getDecoder().decode(licenseSignStr));
        String [] arr = licenseSignStr.split(SignUtil.SPLIT);
        if(arr.length == 3){
            LicenseSign licenseSign = new LicenseSign();
            licenseSign.rsaaespassword = arr[0];
            licenseSign.aesAuth = arr[1];
            licenseSign.aesAuthCheckCode = arr[2];

            return licenseSign;
        }

        return null;
    }


    /**
     * 获取服务器信息
     * @return
     */
    public Map<String, Object> serverinfo() {
        String aespassword = RSAUtil.decryptByPublic(rsaaespassword);

        String auth = AESUtil.deCode(aesAuth, aespassword);
        return JSONObject.parseObject(auth,Map.class);
    }



    public String getAesAuth() {
        return aesAuth;
    }

    public String getAesAuthCheckCode() {
        return aesAuthCheckCode;
    }
}
