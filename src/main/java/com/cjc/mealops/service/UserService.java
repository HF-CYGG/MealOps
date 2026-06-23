package com.cjc.mealops.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjc.mealops.entity.User;
import java.util.Map;

public interface UserService extends IService<User> {
    Map<String, Object> login(Map<String, Object> body);

    void logout();
}
