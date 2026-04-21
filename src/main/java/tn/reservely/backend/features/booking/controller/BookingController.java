package tn.reservely.backend.features.booking.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.reservely.backend.features.booking.dto.BookingResponse;
import tn.reservely.backend.features.booking.dto.CreateBookingRequest;
import tn.reservely.backend.features.booking.service.BookingService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse create(@Valid @RequestBody CreateBookingRequest req, Authentication auth) {
        return bookingService.create(UUID.fromString(auth.getName()), req);
    }

    @GetMapping("/me")
    public List<BookingResponse> myBookings(Authentication auth) {
        return bookingService.getUserBookings(UUID.fromString(auth.getName()));
    }

    @PatchMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@PathVariable UUID id, Authentication auth) {
        bookingService.cancel(id, UUID.fromString(auth.getName()));
    }
}
