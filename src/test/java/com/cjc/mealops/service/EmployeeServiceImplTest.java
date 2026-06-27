package com.cjc.mealops.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cjc.mealops.common.BaseContext;
import com.cjc.mealops.entity.Employee;
import com.cjc.mealops.mapper.EmployeeMapper;
import com.cjc.mealops.service.impl.EmployeeServiceImpl;
import com.cjc.mealops.util.JwtUtils;
import com.cjc.mealops.util.Md5Utils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class EmployeeServiceImplTest {

    @AfterEach
    void tearDown() {
        BaseContext.clear();
    }

    @Test
    void createEmployeeFillsPasswordStatusAndAuditFields() {
        EmployeeMapper mapper = mock(EmployeeMapper.class);
        when(mapper.insert(org.mockito.ArgumentMatchers.any(Employee.class))).thenReturn(1);
        EmployeeServiceImpl service = new EmployeeServiceImpl(
                mapper,
                mock(LoginSecurityService.class),
                mock(JwtUtils.class)
        );
        BaseContext.setCurrentId(99L);

        Employee employee = new Employee();
        employee.setUsername("cashier");
        employee.setName("Cashier");
        employee.setPhone("13900000002");
        employee.setSex("1");
        employee.setIdNumber("110101199001011234");

        assertThat(service.create(employee)).isTrue();

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(mapper).insert(captor.capture());
        Employee saved = captor.getValue();
        assertThat(saved.getPassword()).isEqualTo(Md5Utils.digest("123456"));
        assertThat(saved.getStatus()).isEqualTo(1);
        assertThat(saved.getCreateUser()).isEqualTo(99L);
        assertThat(saved.getUpdateUser()).isEqualTo(99L);
        assertThat(saved.getCreateTime()).isNotNull();
        assertThat(saved.getUpdateTime()).isNotNull();
    }
}
