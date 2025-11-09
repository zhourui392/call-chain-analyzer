package com.example.user.provider;

import com.example.user.api.UserInfoService;
import com.example.user.model.User;
import com.example.user.repository.UserRepository;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

@DubboService(version = "1.0.0", group = "ecommerce")
public class UserInfoServiceImpl implements UserInfoService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User getUserInfo(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public User getUserContacts(Long userId) {
        // 返回用户联系方式（email, phone）
        return userRepository.findById(userId);
    }
}
