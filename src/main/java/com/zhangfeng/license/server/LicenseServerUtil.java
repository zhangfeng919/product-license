package com.zhangfeng.license.server;

import com.alibaba.fastjson.JSONObject;
import com.zhangfeng.license.AESUtil;
import com.zhangfeng.license.SignUtil;

import java.util.Base64;

public class LicenseServerUtil {


    private static String aespassword;

    public static String createAuthCode(JSONObject campLicenseAuth){

        if(campLicenseAuth == null){
            return null;
        }

        String authStr = JSONObject.toJSONString(campLicenseAuth);

        String rsaaespassword = RSAPrivateUtil.encryptByPrivate(aespassword);
        String aesbody = AESUtil.enCode(authStr, aespassword);
        String checknum = RSAPrivateUtil.sign(aesbody);

        String result = new StringBuffer().append(rsaaespassword).append(SignUtil.SPLIT).
                append(aesbody).append(SignUtil.SPLIT).
                append(checknum).toString();
        return Base64.getEncoder().encodeToString(result.getBytes());
    }


    public void setAespassword(String aespassword) {
        LicenseServerUtil.aespassword = aespassword;
    }
}
