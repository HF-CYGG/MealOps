package com.cjc.mealops.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cjc.mealops.entity.Employee;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface EmployeeMapper extends BaseMapper<Employee> {
    @Select("select * from employee where username = #{username} limit 1")
    Employee selectByUsername(@Param("username") String username);

    @Select("""
            select * from employee
            where username = #{identifier}
               or phone = #{identifier}
               or id_number = #{identifier}
            limit 1
            """)
    Employee selectByLoginIdentifier(@Param("identifier") String identifier);
}
