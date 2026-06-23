package com.cjc.mealops.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.entity.User;
import com.cjc.mealops.mapper.UserMapper;
import com.cjc.mealops.service.UserService;
import com.cjc.mealops.util.JwtUtils;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final JwtUtils jwtUtils;

    public UserServiceImpl(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Map<String, Object> login(Map<String, Object> body) {
        String phone = textValue(body.get("phone"));
        if (!StringUtils.hasText(phone)) {
            phone = textValue(body.get("username"));
        }
        if (!StringUtils.hasText(phone)) {
            throw new BusinessException("Phone is required");
        }

        User user = lambdaQuery()
                .eq(User::getPhone, phone)
                .one();
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            String name = textValue(body.get("name"));
            user.setName(StringUtils.hasText(name) ? name : phone);
            user.setCreateTime(LocalDateTime.now());
            save(user);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", user.getId());
        result.put("phone", user.getPhone());
        result.put("name", user.getName());
        result.put("token", jwtUtils.generate(user.getId(), "USER"));
        return result;
    }

    @Override
    public void logout() {
        // Stateless JWT logout is handled by the client discarding the token.
    }

    private String textValue(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }
}
