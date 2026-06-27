package com.cjc.mealops.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.cjc.mealops.common.BaseContext;
import com.cjc.mealops.common.BusinessException;
import com.cjc.mealops.entity.AddressBook;
import com.cjc.mealops.mapper.AddressBookMapper;
import com.cjc.mealops.mapper.OrdersMapper;
import com.cjc.mealops.service.impl.AddressBookServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AddressBookServiceTest {
    @Mock
    private AddressBookMapper addressBookMapper;
    @Mock
    private OrdersMapper ordersMapper;

    @AfterEach
    void clearContext() {
        BaseContext.clear();
    }

    @Test
    void userCanOnlyUpdateOwnAddress() {
        BaseContext.setCurrentId(101L);
        AddressBook existing = new AddressBook();
        existing.setId(9L);
        existing.setUserId(101L);
        when(addressBookMapper.selectById(9L)).thenReturn(existing);
        AddressBookServiceImpl service = service();

        AddressBook update = new AddressBook();
        update.setId(9L);
        update.setUserId(202L);
        update.setConsignee("新收货人");
        service.update(update);

        ArgumentCaptor<AddressBook> captor = ArgumentCaptor.forClass(AddressBook.class);
        verify(addressBookMapper).updateById(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getUserId()).isEqualTo(101L);
    }

    @Test
    void userCannotUpdateAnotherUsersAddress() {
        BaseContext.setCurrentId(101L);
        AddressBook existing = new AddressBook();
        existing.setId(9L);
        existing.setUserId(202L);
        when(addressBookMapper.selectById(9L)).thenReturn(existing);
        AddressBookServiceImpl service = service();

        AddressBook update = new AddressBook();
        update.setId(9L);
        assertThatThrownBy(() -> service.update(update))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Address not found");
        verify(addressBookMapper, never()).updateById(any(AddressBook.class));
    }

    @Test
    void userCannotDeleteAnotherUsersAddress() {
        BaseContext.setCurrentId(101L);
        AddressBook existing = new AddressBook();
        existing.setId(9L);
        existing.setUserId(202L);
        when(addressBookMapper.selectById(9L)).thenReturn(existing);
        AddressBookServiceImpl service = service();

        assertThatThrownBy(() -> service.delete(9L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Address not found");
        verify(addressBookMapper, never()).deleteById(9L);
        verifyNoInteractions(ordersMapper);
    }

    @Test
    void userCannotReadAnotherUsersAddressDetail() {
        BaseContext.setCurrentId(101L);
        AddressBook existing = new AddressBook();
        existing.setId(9L);
        existing.setUserId(202L);
        when(addressBookMapper.selectById(9L)).thenReturn(existing);
        AddressBookServiceImpl service = service();

        assertThatThrownBy(() -> service.getById(9L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Address not found");
    }

    @Test
    void deleteClearsOwnedOrderAddressReferencesBeforeRemovingAddress() {
        BaseContext.setCurrentId(101L);
        AddressBook existing = new AddressBook();
        existing.setId(9L);
        existing.setUserId(101L);
        when(addressBookMapper.selectById(9L)).thenReturn(existing);
        when(addressBookMapper.deleteById(9L)).thenReturn(1);
        AddressBookServiceImpl service = service();

        org.assertj.core.api.Assertions.assertThat(service.delete(9L)).isTrue();

        InOrder inOrder = inOrder(ordersMapper, addressBookMapper);
        inOrder.verify(ordersMapper).clearAddressBookReference(101L, 9L);
        inOrder.verify(addressBookMapper).deleteById(9L);
    }

    @Test
    void deleteThrowsWhenAddressRemovalFailsAfterClearingOrderReferences() {
        BaseContext.setCurrentId(101L);
        AddressBook existing = new AddressBook();
        existing.setId(9L);
        existing.setUserId(101L);
        when(addressBookMapper.selectById(9L)).thenReturn(existing);
        when(addressBookMapper.deleteById(9L)).thenReturn(0);
        AddressBookServiceImpl service = service();

        assertThatThrownBy(() -> service.delete(9L))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Address delete failed");

        InOrder inOrder = inOrder(ordersMapper, addressBookMapper);
        inOrder.verify(ordersMapper).clearAddressBookReference(101L, 9L);
        inOrder.verify(addressBookMapper).deleteById(9L);
    }

    private AddressBookServiceImpl service() {
        AddressBookServiceImpl service = new AddressBookServiceImpl(ordersMapper);
        ReflectionTestUtils.setField(service, "baseMapper", addressBookMapper);
        return service;
    }
}
