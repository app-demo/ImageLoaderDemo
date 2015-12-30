package com.cn.image;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Author: river
 * Date: 2015/12/29 10:33
 * Description:
 */
public class HttpUtils {
    /**
     * 下载
     *
     * @param key
     * @return
     * @throws IOException
     */
    public static InputStream download(String key) throws IOException {

        HttpURLConnection conn = (HttpURLConnection) new URL(key).openConnection();

        return conn.getInputStream();

    }
}
