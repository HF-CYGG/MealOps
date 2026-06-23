package com.cjc.mealops.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.dto.EmployeeLoginDTO;
import com.cjc.mealops.entity.Employee;
import com.cjc.mealops.mapper.EmployeeMapper;
import com.cjc.mealops.service.EmployeeService;
import com.cjc.mealops.service.LoginSecurityService;
import com.cjc.mealops.util.JwtUtils;
import com.cjc.mealops.util.Md5Utils;
import com.cjc.mealops.vo.EmployeeLoginVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
    private final EmployeeMapper employeeMapper;
    private final LoginSecurityService loginSecurityService;
    private final JwtUtils jwtUtils;

    public EmployeeServiceImpl(EmployeeMapper employeeMapper,
                               LoginSecurityService loginSecurityService,
                               JwtUtils jwtUtils) {
        this.employeeMapper = employeeMapper;
        this.loginSecurityService = loginSecurityService;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public Employee login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BusinessException("Username and password are required");
        }
        if (loginSecurityService.isLocked(username)) {
            throw new BusinessException("Account is locked");
        }

        Employee employee = employeeMapper.selectByUsername(username);
        if (employee == null || !Md5Utils.digest(password).equals(employee.getPassword())) {
            loginSecurityService.recordFailure(username);
            throw new BusinessException("Invalid username or password");
        }
        if (employee.getStatus() != null && employee.getStatus() == 0) {
            throw new BusinessException("Account is disabled");
        }

        loginSecurityService.clear(username);
        return employee;
    }

    @Override
    public EmployeeLoginVO login(EmployeeLoginDTO loginDTO) {
        Employee employee = login(loginDTO.getUsername(), loginDTO.getPassword());
        String token = jwtUtils.generate(employee.getId(), "EMPLOYEE");
        return new EmployeeLoginVO(employee.getId(), employee.getUsername(), employee.getName(), token);
    }

    @Override
    public void logout() {
        // JWT is stateless; client removes the token.
    }

    @Override
    public void updateStatus(Integer status, Long id) {
        Employee employee = new Employee();
        employee.setId(id);
        employee.setStatus(status);
        updateById(employee);
    }
}
