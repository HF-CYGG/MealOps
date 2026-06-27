package com.cjc.mealops.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.entity.Setmeal;
import com.cjc.mealops.mapper.CategoryMapper;
import com.cjc.mealops.mapper.SetmealDishMapper;
import com.cjc.mealops.mapper.SetmealMapper;
import com.cjc.mealops.service.impl.SetmealServiceImpl;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SetmealServiceImplTest {
    @Test
    void rejectsStatusUpdateWhenSetmealDoesNotExist() {
        SetmealMapper setmealMapper = mock(SetmealMapper.class);
        when(setmealMapper.updateById(org.mockito.ArgumentMatchers.any(Setmeal.class))).thenReturn(0);
        SetmealServiceImpl service = new SetmealServiceImpl(
                mock(SetmealDishMapper.class),
                mock(CategoryMapper.class)
        );
        ReflectionTestUtils.setField(service, "baseMapper", setmealMapper);

        assertThatThrownBy(() -> service.updateStatus(0, List.of(5001L)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Setmeal not found");
    }
}
