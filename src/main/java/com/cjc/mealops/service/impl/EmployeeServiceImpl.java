package com.cjc.mealops.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjc.mealops.common.BaseContext;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.dto.EmployeeLoginDTO;
import com.cjc.mealops.entity.Employee;
import com.cjc.mealops.mapper.EmployeeMapper;
import com.cjc.mealops.service.EmployeeService;
import com.cjc.mealops.service.LoginSecurityService;
import com.cjc.mealops.util.JwtUtils;
import com.cjc.mealops.util.Md5Utils;
import com.cjc.mealops.vo.EmployeeLoginVO;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
    private static final long SYSTEM_USER_ID = 0L;

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
    public boolean create(Employee employee) {
        if (!StringUtils.hasText(employee.getPassword())) {
            throw new BusinessException("Password is required");
        }
        LocalDateTime now = LocalDateTime.now();
        Long operatorId = BaseContext.getCurrentId() == null ? SYSTEM_USER_ID : BaseContext.getCurrentId();

        employee.setPassword(Md5Utils.digest(employee.getPassword().trim()));
        employee.setPhone(trimToNull(employee.getPhone()));
        employee.setIdNumber(trimToNull(employee.getIdNumber()));
        if (employee.getStatus() == null) {
            employee.setStatus(1);
        }
        employee.setCreateTime(now);
        employee.setUpdateTime(now);
        employee.setCreateUser(operatorId);
        employee.setUpdateUser(operatorId);
        return employeeMapper.insert(employee) > 0;
    }

    @Override
    public Employee login(String username, String password) {
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BusinessException("Username and password are required");
        }
        String identifier = username.trim();
        if (loginSecurityService.isLocked(identifier)) {
            throw new BusinessException("Account is locked");
        }

        Employee employee = employeeMapper.selectByLoginIdentifier(identifier);
        if (employee == null || !Md5Utils.digest(password).equals(employee.getPassword())) {
            loginSecurityService.recordFailure(identifier);
            throw new BusinessException("Invalid username or password");
        }
        if (employee.getStatus() != null && employee.getStatus() == 0) {
            throw new BusinessException("Account is disabled");
        }

        loginSecurityService.clear(identifier);
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

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
