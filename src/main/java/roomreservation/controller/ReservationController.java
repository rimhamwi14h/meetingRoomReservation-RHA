package roomreservation.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import roomreservation.model.Reservation;
import roomreservation.request.CreateReservationRequest;
import roomreservation.service.ReservationService;
import roomreservation.request.AutomaticReservationRequest;
import java.time.OffsetDateTime;
import java.util.List;
import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    @Autowired
    private ReservationService reservationService;
    @PostMapping
    public Reservation createReservation(
            @Valid @RequestBody CreateReservationRequest request) {

        return reservationService.createReservation(request);
    }
    @PatchMapping("/{id}/cancel")
    public Reservation cancelReservation(@PathVariable Long id) {
        return reservationService.cancelReservation(id);
    }
    @GetMapping("/{id}")
    public Reservation getReservation(@PathVariable Long id) {
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
    public Reservation createAutomaticReservation(
            @Valid @RequestBody AutomaticReservationRequest request) {

        return reservationService.createAutomaticReservation(request);
    }
}