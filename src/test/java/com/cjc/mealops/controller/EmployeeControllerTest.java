package com.cjc.mealops.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.cjc.mealops.common.R;
import com.cjc.mealops.entity.Employee;
import com.cjc.mealops.service.EmployeeService;
import org.junit.jupiter.api.Test;

class EmployeeControllerTest {

    @Test
    void createDelegatesToEmployeeCreateServiceMethod() {
        ApiInvokeSupport api = mock(ApiInvokeSupport.class);
        EmployeeService employeeService = mock(EmployeeService.class);
        Employee employee = new Employee();
        employee.setUsername("cashier");
        employee.setName("Cashier");
        when(employeeService.create(employee)).thenReturn(true);

        R<Boolean> response = new EmployeeController(api, employeeService).create(employee);

        assertThat(response.getCode()).isEqualTo(1);
        assertThat(response.getData()).isTrue();
        verify(employeeService).create(employee);
    }
}
