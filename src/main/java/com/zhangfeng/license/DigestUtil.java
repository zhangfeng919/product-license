package com.zhangfeng.license;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA3Digest;
import org.bouncycastle.util.encoders.Hex;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @ClassName DigestUtil
 * @Description //TODO
 * @Author zhangfeng
 * @Date 2020/7/9 15:18
 * @Version 1.0
 **/
public class DigestUtil {

    public enum DigestType{
        MD5("MD5"),SHA1("SHA1"),SHA2512("SHA-512");

        private String code;

        DigestType(String code){
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    public enum DigestSHA3Type{
        SHA256(256),SHA512(512);

        private int code;

        DigestSHA3Type(int code){
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }


    public static String md5(String source){
        return enCode(DigestType.MD5, source);
    }

    public static String sha1(String source){
        return enCode(DigestType.SHA1, source);
    }

    public static String sha2(String source){
        return enCode(DigestType.SHA2512, source);
    }

    public static String sha3(String source){
        return enCodeSHA3(DigestSHA3Type.SHA512, source);
    }

    private static String enCode(DigestType digestType, String source){
        try {
            MessageDigest md = MessageDigest.getInstance(digestType.getCode());
            md.update(source.getBytes(Charset.forName(CharsetType.utf8.name())));
            return new BigInteger(1,md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String enCodeSHA3(DigestSHA3Type digestType, String source){

        byte [] bytes = new byte[0];
        try {
            bytes = source.getBytes(CharsetType.utf8.name());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Digest digest = new SHA3Digest(digestType.code);
        digest.update(bytes, 0, bytes.length);
        byte[] rsData = new byte[digest.getDigestSize()];
        digest.doFinal(rsData, 0);
        return Hex.toHexString(rsData);
    }


    public static void main(String[] args) {
        LogUtil.info(sha3("hahah"));
        String pwd = "123456";
        LogUtil.info(DigestUtil.md5(pwd));
        LogUtil.info(DigestUtil.sha1(pwd));
        LogUtil.info(DigestUtil.sha2(pwd));
        LogUtil.info(DigestUtil.sha3(pwd));
    }
}
