package com.cjc.mealops.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.dto.DiningTableDTO;
import com.cjc.mealops.entity.DiningTable;
import com.cjc.mealops.mapper.DiningTableMapper;
import com.cjc.mealops.service.DiningTableService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class DiningTableServiceImpl extends ServiceImpl<DiningTableMapper, DiningTable>
        implements DiningTableService {
    private final DiningTableMapper diningTableMapper;

    public DiningTableServiceImpl(DiningTableMapper diningTableMapper) {
        this.diningTableMapper = diningTableMapper;
    }

    @Override
    public Object page(Map<String, Object> params) {
        Page<DiningTable> page = new Page<>(longParam(params, "page", 1L), longParam(params, "pageSize", 10L));
        LambdaQueryWrapper<DiningTable> wrapper = new LambdaQueryWrapper<>();
        Object status = params == null ? null : params.get("status");
        Object name = params == null ? null : params.get("name");
        if (status != null && !String.valueOf(status).isBlank()) {
            wrapper.eq(DiningTable::getStatus, String.valueOf(status));
        }
        if (name != null && !String.valueOf(name).isBlank()) {
            wrapper.and(w -> w.like(DiningTable::getTableNo, name).or().like(DiningTable::getTableName, name));
        }
        wrapper.orderByAsc(DiningTable::getTableNo);
        return diningTableMapper.selectPage(page, wrapper);
    }

    @Override
    public DiningTable create(DiningTableDTO dto) {
        DiningTable table = new DiningTable();
        BeanUtils.copyProperties(dto, table);
        if (!StringUtils.hasText(table.getTableName())) {
            table.setTableName(table.getTableNo());
        }
        if (!StringUtils.hasText(table.getStatus())) {
            table.setStatus(DiningTable.AVAILABLE);
        }
        LocalDateTime now = LocalDateTime.now();
        table.setCreateTime(now);
        table.setUpdateTime(now);
        diningTableMapper.insert(table);
        return table;
    }

    @Override
    public DiningTable update(DiningTableDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException("Dining table id is required");
        }
        DiningTable table = new DiningTable();
        BeanUtils.copyProperties(dto, table);
        table.setUpdateTime(LocalDateTime.now());
        diningTableMapper.updateById(table);
        return diningTableMapper.selectById(dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String status, List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        for (Long id : ids) {
            DiningTable table = new DiningTable();
            table.setId(id);
            table.setStatus(status);
            table.setUpdateTime(LocalDateTime.now());
            diningTableMapper.updateById(table);
        }
    }

    private long longParam(Map<String, Object> params, String name, long defaultValue) {
        if (params == null || params.get(name) == null || String.valueOf(params.get(name)).isBlank()) {
            return defaultValue;
        }
        return Long.parseLong(String.valueOf(params.get(name)));
    }
}
