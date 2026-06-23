package com.cjc.mealops.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cjc.mealops.entity.AddressBook;
import java.util.List;

public interface AddressBookService extends IService<AddressBook> {
    AddressBook create(AddressBook addressBook);

    List<AddressBook> listCurrentUser();

    AddressBook getDefault();

    void setDefault(Long id);
}
