package roomreservation.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import roomreservation.model.Reservation;
import roomreservation.request.AutomaticReservationRequest;
import roomreservation.request.CreateReservationRequest;
import roomreservation.service.ReservationService;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @Valid @RequestBody CreateReservationRequest request) {

        Reservation reservation =
                reservationService.createReservation(request);

        URI location = URI.create(
                "/api/reservations/" + reservation.getId()
        );

        return ResponseEntity
                .created(location)
                .body(reservation);
    }

    @PatchMapping("/{id}/cancel")
    public Reservation cancelReservation(
            @PathVariable Long id) {

        return reservationService.cancelReservation(id);
    }

    @GetMapping("/{id}")
    public Reservation getReservation(
            @PathVariable Long id) {

        return reservationService.getReservation(id);
    }

    @GetMapping
    public List<Reservation> getReservations(
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) Long organizerId,
            @RequestParam(required = false) OffsetDateTime from,
            @RequestParam(required = false) OffsetDateTime to) {

        return reservationService.getReservations(
                roomId,
                organizerId,
                from,
                to
        );
    }

    @PostMapping("/automatic")
    public ResponseEntity<Reservation> createAutomaticReservation(
            @Valid @RequestBody AutomaticReservationRequest request) {

        Reservation reservation =
                reservationService.createAutomaticReservation(request);

        URI location = URI.create(
                "/api/reservations/" + reservation.getId()
        );

        return ResponseEntity
                .created(location)
                .body(reservation);
    }
}