package tn.reservely.backend.features.business.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.reservely.backend.features.booking.dto.SlotDto;
import tn.reservely.backend.features.business.dto.BusinessResponse;
import tn.reservely.backend.features.business.service.BusinessService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/businesses")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @GetMapping
    public List<BusinessResponse> list(@RequestParam(required = false) String city) {
        return businessService.listAll(city);
    }

    @GetMapping("/{id}")
    public BusinessResponse detail(@PathVariable UUID id) {
        return businessService.getById(id);
    }

    @GetMapping("/{id}/slots")
    public List<SlotDto> slots(
            @PathVariable UUID id,
            @RequestParam String date,
            @RequestParam UUID serviceId,
            @RequestParam(required = false) UUID staffId) {
        return businessService.getSlots(id, date, serviceId, staffId);
    }
}
