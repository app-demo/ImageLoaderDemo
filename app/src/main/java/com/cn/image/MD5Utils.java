package com.cn.image;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Author: river
 * Date: 2015/12/29 13:58
 * Description:
 */
public class MD5Utils {

    /**
     * MD5加密
     *
     * @param key
     * @return
     */
    public static String decode(String key) {

        MessageDigest messageDigest = null;

        try {
            messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();

            //utf-8编码
            messageDigest.update(key.getBytes("utf-8"));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        byte[] byteArray = messageDigest.digest();
        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < byteArray.length; i++) {

            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1) {
                buffer.append("0").append(Integer.toHexString(0xFF & byteArray[i]));
            } else {
                buffer.append(Integer.toHexString(0xFF & byteArray[i]));
            }
        }

        return buffer.toString();

    }
}
