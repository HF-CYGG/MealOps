package com.cjc.mealops.config;

import com.cjc.mealops.entity.Employee;
import com.cjc.mealops.mapper.EmployeeMapper;
import com.cjc.mealops.util.Md5Utils;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Order(1)
public class AdminAccountInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminAccountInitializer.class);
    private static final long SYSTEM_USER_ID = 0L;

    private final EmployeeMapper employeeMapper;
    private final AdminAccountProperties properties;

    public AdminAccountInitializer(EmployeeMapper employeeMapper, AdminAccountProperties properties) {
        this.employeeMapper = employeeMapper;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        run();
    }

    public void run() {
        if (!properties.isEnabled()) {
            log.info("Admin account bootstrap is disabled");
            return;
        }
        validate();

        String username = properties.getUsername().trim();
        Employee existing = employeeMapper.selectByUsername(username);
        if (existing == null) {
            employeeMapper.insert(newEmployee(username));
            log.info("Configured admin account '{}' created", username);
            return;
        }

        employeeMapper.updateById(updatedEmployee(existing.getId(), username));
        log.info("Configured admin account '{}' updated", username);
    }

    private void validate() {
        if (!StringUtils.hasText(properties.getUsername())) {
            throw new IllegalStateException("mealops.admin.username must not be blank");
        }
        if (!StringUtils.hasText(properties.getPassword())) {
            throw new IllegalStateException("mealops.admin.password must not be blank");
        }
        if (!StringUtils.hasText(properties.getName())) {
            throw new IllegalStateException("mealops.admin.name must not be blank");
        }
    }

    private Employee newEmployee(String username) {
        LocalDateTime now = LocalDateTime.now();
        Employee employee = baseEmployee(username);
        employee.setCreateTime(now);
        employee.setUpdateTime(now);
        employee.setCreateUser(SYSTEM_USER_ID);
        employee.setUpdateUser(SYSTEM_USER_ID);
        return employee;
    }

    private Employee updatedEmployee(Long id, String username) {
        Employee employee = baseEmployee(username);
        employee.setId(id);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(SYSTEM_USER_ID);
        return employee;
    }

    private Employee baseEmployee(String username) {
        Employee employee = new Employee();
        employee.setUsername(username);
        employee.setPassword(Md5Utils.digest(properties.getPassword()));
        employee.setName(properties.getName().trim());
        employee.setPhone(trimToNull(properties.getPhone()));
        employee.setStatus(1);
        return employee;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
