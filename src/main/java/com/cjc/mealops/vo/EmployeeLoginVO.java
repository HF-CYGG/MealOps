package com.cjc.mealops.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeLoginVO {
    private Long id;
    private String userName;
    private String name;
    private String token;
}
