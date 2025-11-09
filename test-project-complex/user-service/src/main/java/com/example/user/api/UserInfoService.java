package com.example.user.api;

import com.example.user.model.User;

/**
 * 用户信息服务 Dubbo 接口
 * 提供给其他服务调用
 */
public interface UserInfoService {

    /**
     * 获取用户信息
     */
    User getUserInfo(Long userId);

    /**
     * 获取用户联系方式
     */
    User getUserContacts(Long userId);
}
