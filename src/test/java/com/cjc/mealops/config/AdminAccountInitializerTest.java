package com.cjc.mealops.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cjc.mealops.entity.Employee;
import com.cjc.mealops.mapper.EmployeeMapper;
import com.cjc.mealops.util.Md5Utils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AdminAccountInitializerTest {

    @Test
    void createsConfiguredAdminWhenUsernameDoesNotExist() {
        EmployeeMapper mapper = org.mockito.Mockito.mock(EmployeeMapper.class);
        AdminAccountProperties properties = configuredProperties();
        when(mapper.selectByUsername("owner")).thenReturn(null);

        new AdminAccountInitializer(mapper, properties).run();

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(mapper).insert(captor.capture());
        Employee employee = captor.getValue();
        assertThat(employee.getUsername()).isEqualTo("owner");
        assertThat(employee.getPassword()).isEqualTo(Md5Utils.digest("secret123"));
        assertThat(employee.getName()).isEqualTo("Store Owner");
        assertThat(employee.getPhone()).isEqualTo("13900000001");
        assertThat(employee.getStatus()).isEqualTo(1);
        assertThat(employee.getCreateUser()).isEqualTo(0L);
        assertThat(employee.getUpdateUser()).isEqualTo(0L);
        assertThat(employee.getCreateTime()).isNotNull();
        assertThat(employee.getUpdateTime()).isNotNull();
    }

    @Test
    void updatesConfiguredAdminWhenUsernameAlreadyExists() {
        EmployeeMapper mapper = org.mockito.Mockito.mock(EmployeeMapper.class);
        AdminAccountProperties properties = configuredProperties();
        Employee existing = new Employee();
        existing.setId(42L);
        existing.setUsername("owner");
        existing.setStatus(0);
        when(mapper.selectByUsername("owner")).thenReturn(existing);

        new AdminAccountInitializer(mapper, properties).run();

        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(mapper).updateById(captor.capture());
        Employee employee = captor.getValue();
        assertThat(employee.getId()).isEqualTo(42L);
        assertThat(employee.getUsername()).isEqualTo("owner");
        assertThat(employee.getPassword()).isEqualTo(Md5Utils.digest("secret123"));
        assertThat(employee.getName()).isEqualTo("Store Owner");
        assertThat(employee.getPhone()).isEqualTo("13900000001");
        assertThat(employee.getStatus()).isEqualTo(1);
        assertThat(employee.getUpdateUser()).isEqualTo(0L);
        assertThat(employee.getUpdateTime()).isNotNull();
        verify(mapper, never()).insert(any(Employee.class));
    }

    @Test
    void skipsDatabaseAccessWhenDisabled() {
        EmployeeMapper mapper = org.mockito.Mockito.mock(EmployeeMapper.class);
        AdminAccountProperties properties = configuredProperties();
        properties.setEnabled(false);

        new AdminAccountInitializer(mapper, properties).run();

        verify(mapper, never()).selectByUsername(any());
        verify(mapper, never()).insert(any(Employee.class));
        verify(mapper, never()).updateById(any(Employee.class));
    }

    private static AdminAccountProperties configuredProperties() {
        AdminAccountProperties properties = new AdminAccountProperties();
        properties.setEnabled(true);
        properties.setUsername("owner");
        properties.setPassword("secret123");
        properties.setName("Store Owner");
        properties.setPhone("13900000001");
        return properties;
    }
}
