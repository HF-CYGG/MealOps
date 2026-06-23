package com.cjc.mealops.controller;

import com.cjc.mealops.common.R;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/addressBook")
public class AddressBookController {
    private final ApiInvokeSupport api;

    public AddressBookController(ApiInvokeSupport api) {
        this.api = api;
    }

    @PostMapping
    public R<Object> create(@RequestBody Map<String, Object> body) {
        return R.success(api.invoke("addressBookService", List.of("create", "save", "add"), body));
    }

    @GetMapping("/list")
    public R<Object> list() {
        return R.success(api.invoke("addressBookService", List.of("list", "listCurrentUser")));
    }

    @GetMapping("/{id}")
    public R<Object> getById(@PathVariable Long id) {
        return R.success(api.invoke("addressBookService", List.of("getById", "detail"), id));
    }

    @PutMapping
    public R<Object> update(@RequestBody Map<String, Object> body) {
        return R.success(api.invoke("addressBookService", List.of("update", "updateById"), body));
    }

    @DeleteMapping
    public R<Object> delete(@RequestParam Long id) {
        return R.success(api.invoke("addressBookService", List.of("delete", "remove", "removeById"), id));
    }

    @PutMapping("/default")
    public R<Object> setDefault(@RequestBody Map<String, Object> body) {
        return R.success(api.invoke("addressBookService", List.of("setDefault", "defaultAddress"), Long.parseLong(String.valueOf(body.get("id")))));
    }

    @GetMapping("/default")
    public R<Object> getDefault() {
        return R.success(api.invoke("addressBookService", List.of("getDefault", "defaultAddress")));
    }
}
