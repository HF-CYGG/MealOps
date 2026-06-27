package com.cjc.mealops.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cjc.mealops.common.BaseContext;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.entity.AddressBook;
import com.cjc.mealops.mapper.AddressBookMapper;
import com.cjc.mealops.mapper.OrdersMapper;
import com.cjc.mealops.service.AddressBookService;
import java.io.Serializable;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddressBookServiceImpl extends ServiceImpl<AddressBookMapper, AddressBook>
        implements AddressBookService {
    private final OrdersMapper ordersMapper;

    public AddressBookServiceImpl(OrdersMapper ordersMapper) {
        this.ordersMapper = ordersMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AddressBook create(AddressBook addressBook) {
        Long userId = currentUserId();
        addressBook.setUserId(userId);
        addressBook.setIsDefault(0);
        save(addressBook);
        return addressBook;
    }

    @Override
    public List<AddressBook> list() {
        return listCurrentUser();
    }

    @Override
    public List<AddressBook> listCurrentUser() {
        Long userId = currentUserId();
        return lambdaQuery()
                .eq(AddressBook::getUserId, userId)
                .list();
    }

    @Override
    public AddressBook getById(Serializable id) {
        Long userId = currentUserId();
        if (id == null) {
            throw new BusinessException("Address not found");
        }
        AddressBook addressBook = super.getById(id);
        if (addressBook == null || !userId.equals(addressBook.getUserId())) {
            throw new BusinessException("Address not found");
        }
        return addressBook;
    }

    @Override
    public AddressBook getDefault() {
        Long userId = currentUserId();
        return lambdaQuery()
                .eq(AddressBook::getUserId, userId)
                .eq(AddressBook::getIsDefault, 1)
                .one();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long id) {
        Long userId = currentUserId();
        AddressBook addressBook = getById(id);
        if (addressBook == null || !userId.equals(addressBook.getUserId())) {
            throw new BusinessException("Address not found");
        }
        lambdaUpdate()
                .eq(AddressBook::getUserId, userId)
                .set(AddressBook::getIsDefault, 0)
                .update();
        addressBook.setIsDefault(1);
        updateById(addressBook);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(AddressBook addressBook) {
        Long userId = currentUserId();
        AddressBook existing = getOwnedAddress(addressBook.getId(), userId);
        addressBook.setUserId(userId);
        if (addressBook.getIsDefault() == null) {
            addressBook.setIsDefault(existing.getIsDefault());
        }
        return updateById(addressBook);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        Long userId = currentUserId();
        getOwnedAddress(id, userId);
        ordersMapper.clearAddressBookReference(userId, id);
        if (!removeById(id)) {
            throw new BusinessException("Address delete failed");
        }
        return true;
    }

    private AddressBook getOwnedAddress(Long id, Long userId) {
        if (id == null) {
            throw new BusinessException("Address not found");
        }
        AddressBook addressBook = getById(id);
        if (addressBook == null || !userId.equals(addressBook.getUserId())) {
            throw new BusinessException("Address not found");
        }
        return addressBook;
    }

    private Long currentUserId() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            throw new BusinessException("Current user is required");
        }
        return userId;
    }
}
