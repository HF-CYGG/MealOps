package com.cjc.mealops.service;

import java.util.List;
import java.util.Map;

public interface CatalogService {
    List<Map<String, Object>> search(Map<String, Object> params);

    Map<String, Object> getItem(String type, Long id);
}
