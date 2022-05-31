package com.qiusm.getway.token;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;


import java.util.*;

/**
 * @author qiushengming
 */
@Component
public class JwtUtil {

    /**
     * 加密 解密时的密钥 用来生成key
     */
    public static final String JWT_KEY = "limengya";

    /**
     * 生成加密后的秘钥 secretKey
     */
    public static SecretKey generalKey() {
        byte[] encodedKey = Base64.getDecoder().decode(JwtUtil.JWT_KEY);
        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
    }


    public static String create(String id, String subject, long ttlMillis) {

        //指定签名的时候使用的签名算法，也就是header那部分，jjwt
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        // 已经将这部分内容封装好了。//生成JWT的时间
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        //生成签名的时候使用的秘钥secret,
        SecretKey key = generalKey();
        // 这个方法本地封装了的，一般可以从本地配置文件中读取，切记这个秘钥不能外露哦。它就是你服务端的私钥，在任何场景都不应该流露出去。一旦客户端得知这个secret, 那就意味着客户端是可以自我签发jwt了。
        //这里其实就是new一个JwtBuilder，设置jwt的body
        JwtBuilder builder = Jwts.builder()
                //                .setClaims(claims)
                // 如果有私有声明，一定要先设置这个自己创建的私有的声明，这个是给builder的claim赋值，一旦写在标准的声明赋值之后，就是覆盖了那些标准的声明的
                //设置jti(JWT ID)：是JWT的唯一标识，根据业务需要，这个可以设置为一个不重复的值，主要用来作为一次性token,从而回避重放攻击。
                .setId(id)
                //iat: jwt的签发时间
                .setIssuedAt(now)
                .setSubject(subject)
                //sub(Subject)：代表这个JWT的主体，即它的所有人，这个是一个json格式的字符串，可以存放什么userid，roldid
                // 之类的，作为什么用户的唯一标志。//设置签名使用的签名算法和签名使用的秘钥
                .signWith(signatureAlgorithm, key);
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            //设置过期时间
            builder.setExpiration(exp);
        }
        //就开始压缩为xxxxxxxxxxxxxx.xxxxxxxxxxxxxxx.xxxxxxxxxxxxx这样的jwt
        return builder.compact();
    }

    public static Claims parse(String jwt) {

        //签名秘钥，和生成的签名的秘钥一模一样
        SecretKey key = generalKey();
        //得到DefaultJwtParser
        return Jwts.parser()
                //设置签名的秘钥
                .setSigningKey(key)
                //设置需要解析的jwt
                .parseClaimsJws(jwt).getBody();
    }

    public static void main(String[] args) {

        Map<String, Object> user = new HashMap<>();
        user.put("username", "it1995");
        user.put("password", "123456");
        String jwt = create(UUID.randomUUID().toString(), JSON.toJSONString(user),  1000 * 3600 * 24);

        System.out.println("加密后：" + jwt);

        //解密
        Claims claims = parse(jwt);
        System.out.println("解密后：" + claims.getSubject());
    }
}