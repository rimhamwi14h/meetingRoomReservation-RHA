package roomreservation.response;

import lombok.Data;
import roomreservation.model.ReservationStatus;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
public class ReservationResponse {

    private Long id;

    private String title;

    private ReservationStatus status;

    private RoomSummaryResponse room;

    private OrganizerSummaryResponse organizer;

    private OffsetDateTime start;

    private OffsetDateTime end;

    private Integer numberOfParticipants;

    private Set<String> requiredEquipmentCodes;

    private OffsetDateTime createdAt;
}