package com.yidong.recruit.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author lzc
 * @date 2021/4/15 11 07
 * discription
 */
public class TimeUtil {

    public static String getCurrentTime(){
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return df.format(new Date());
    }

    public static void main(String[] args) {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        System.out.println(df.format(new Date()));// new Date()为获取当前系统时间
    }

}
