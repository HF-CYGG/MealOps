package com.cjc.mealops.controller;

import com.cjc.mealops.common.R;
import com.cjc.mealops.service.CatalogService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/catalog/items")
public class CatalogController {
    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public R<Object> search(@RequestParam Map<String, String> params) {
        return R.success(catalogService.search(Map.copyOf(params)));
    }

    @GetMapping("/{type}/{id}")
    public R<Object> getItem(@PathVariable String type, @PathVariable Long id) {
        return R.success(catalogService.getItem(type, id));
    }
}
