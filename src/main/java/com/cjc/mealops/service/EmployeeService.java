package com.cjc.mealops.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjc.mealops.dto.EmployeeLoginDTO;
import com.cjc.mealops.entity.Employee;
import com.cjc.mealops.vo.EmployeeLoginVO;

public interface EmployeeService extends IService<Employee> {
    boolean create(Employee employee);

    Employee login(String username, String password);

    EmployeeLoginVO login(EmployeeLoginDTO loginDTO);

    void logout();

    void updateStatus(Integer status, Long id);
}
