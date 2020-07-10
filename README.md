# product-license
一种局域网产品授权验证方式

client需要 调用生成识别码 com.zhangfeng.license.LicenseUtil#createClientCode

将识别码给产品公司，用来生成授权码 com.zhangfeng.license.server.LicenseServerUtil#createAuthCode

客户识别码包含唯一确定客户的信息
授权码包含授权信息。授权时长等

验签方式：rsa加密aes秘钥。aes加密授权信息，rsa生成签名
