package com.muiboot.shiro.system.controller;

/**
 * Created by 75631 on 2018/10/4.
 */
public interface SysConstant {
    public static final Long BASE_ROLE_KEY=80L;//用户初始角色
    public static final String INIT_USER_PWD="111111";//用户初始角色
    public static final long PAGE_MAX_AGE =60*60*24*7;//页面缓存时间-缓存7天
    public static final long DATA_MAX_AGE =5;//数据缓存时间-缓存5秒
}
