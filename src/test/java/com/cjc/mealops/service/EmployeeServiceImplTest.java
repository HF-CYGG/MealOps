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
        employee.setPassword("cashier123");
        employee.setSex("1");

        assertThat(service.create(employee)).isTrue();

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(mapper).insert(captor.capture());
        Employee saved = captor.getValue();
        assertThat(saved.getPassword()).isEqualTo(Md5Utils.digest("cashier123"));
        assertThat(saved.getPhone()).isNull();
        assertThat(saved.getIdNumber()).isNull();
        assertThat(saved.getStatus()).isEqualTo(1);
        assertThat(saved.getCreateUser()).isEqualTo(99L);
        assertThat(saved.getUpdateUser()).isEqualTo(99L);
        assertThat(saved.getCreateTime()).isNotNull();
        assertThat(saved.getUpdateTime()).isNotNull();
    }

    @Test
    void loginAcceptsUsernamePhoneOrIdNumberAsIdentifier() {
        EmployeeMapper mapper = mock(EmployeeMapper.class);
        LoginSecurityService loginSecurityService = mock(LoginSecurityService.class);
        Employee employee = new Employee();
        employee.setId(7L);
        employee.setUsername("cashier");
        employee.setPassword(Md5Utils.digest("cashier123"));
        employee.setStatus(1);
        when(mapper.selectByLoginIdentifier("13900000002")).thenReturn(employee);
        EmployeeServiceImpl service = new EmployeeServiceImpl(
                mapper,
                loginSecurityService,
                mock(JwtUtils.class)
        );

        Employee loggedIn = service.login("13900000002", "cashier123");

        assertThat(loggedIn).isSameAs(employee);
        verify(mapper).selectByLoginIdentifier("13900000002");
        verify(loginSecurityService).clear("13900000002");
    }
}
