package am.trade.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * Temporary mock controller for filters to prevent UI crashes
 */
@RestController
@RequestMapping({"/api/v1/filters", "/v1/filters"})
public class FilterController {

    @GetMapping
    public ResponseEntity<List<Object>> getFilters() {
        return ResponseEntity.ok(Collections.emptyList());
    }
}
