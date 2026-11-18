package dev.weredok.library.backend.api;

import dev.weredok.library.backend.service.OverdueService;
import dev.weredok.library.shared.dto.OverdueDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/overdue")
public class OverdueController {

    private final OverdueService service;

    public OverdueController(OverdueService service) {
        this.service = service;
    }

    @GetMapping
    public List<OverdueDto> list() {
        return service.list();
    }
}
