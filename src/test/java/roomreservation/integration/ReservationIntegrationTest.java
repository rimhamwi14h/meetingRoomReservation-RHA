package roomreservation.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import roomreservation.model.Building;
import roomreservation.model.Organizer;
import roomreservation.model.Room;
import roomreservation.model.RoomStatus;

import roomreservation.repository.BuildingRepository;
import roomreservation.repository.OrganizerRepository;
import roomreservation.repository.ReservationRepository;
import roomreservation.repository.RoomRepository;

import java.time.OffsetDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private OrganizerRepository organizerRepository;

    @Autowired
    private ReservationRepository reservationRepository;


    @BeforeEach
    void setUp() {

        reservationRepository.deleteAll();
        organizerRepository.deleteAll();
        roomRepository.deleteAll();
        buildingRepository.deleteAll();
    }


    @Test
    void shouldCreateAutomaticReservation() throws Exception {

        // GIVEN

        Building building = new Building();
        building.setName("Building A");
        building.setNumberOfFloors(5);

        building =
                buildingRepository.save(building);


        Organizer organizer = new Organizer();
        organizer.setName("Alice Martin");
        organizer.setEmail("alice@example.org");
        organizer.setBuilding(building);
        organizer.setFloor(2);

        organizer =
                organizerRepository.save(organizer);


        Room room = new Room();
        room.setName("Orion");
        room.setBuilding(building);
        room.setFloor(2);
        room.setCapacity(30);
        room.setStatus(RoomStatus.AVAILABLE);
        room.setEquipment(new HashSet<>());

        room =
                roomRepository.save(room);


        OffsetDateTime start =
                OffsetDateTime.now()
                        .plusDays(1)
                        .withNano(0);

        OffsetDateTime end =
                start.plusHours(1);


        String requestBody = """
                {
                  "title": "Automatic meeting",
                  "organizerId": %d,
                  "start": "%s",
                  "end": "%s",
                  "numberOfParticipants": 20,
                  "requiredEquipmentCodes": []
                }
                """.formatted(
                organizer.getId(),
                start,
                end
        );


        // WHEN + THEN

        mockMvc.perform(
                        post("/api/reservations/automatic")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(requestBody)
                )

                .andExpect(
                        status().isCreated()
                )

                .andExpect(
                        header().exists("Location")
                )

                .andExpect(
                        jsonPath("$.title")
                                .value("Automatic meeting")
                )

                .andExpect(
                        jsonPath("$.status")
                                .value("CONFIRMED")
                )

                .andExpect(
                        jsonPath("$.room.id")
                                .value(room.getId())
                )

                .andExpect(
                        jsonPath("$.room.name")
                                .value("Orion")
                )

                .andExpect(
                        jsonPath("$.organizer.id")
                                .value(organizer.getId())
                )

                .andExpect(
                        jsonPath("$.numberOfParticipants")
                                .value(20)
                )

                .andExpect(
                        jsonPath("$.createdAt")
                                .exists()
                );


        // Vérifie aussi que la réservation
        // est vraiment enregistrée en base
        assertEquals(
                1,
                reservationRepository.count()
        );
    }
    @Test
    void shouldRejectConflictingReservation() throws Exception {

        // GIVEN
        Building building = new Building();
        building.setName("Building A");
        building.setNumberOfFloors(5);

        building =
                buildingRepository.save(building);


        Organizer organizer = new Organizer();
        organizer.setName("Alice Martin");
        organizer.setEmail("alice@example.org");
        organizer.setBuilding(building);
        organizer.setFloor(2);

        organizer =
                organizerRepository.save(organizer);


        Room room = new Room();
        room.setName("Orion");
        room.setBuilding(building);
        room.setFloor(2);
        room.setCapacity(30);
        room.setStatus(RoomStatus.AVAILABLE);
        room.setEquipment(new HashSet<>());

        room =
                roomRepository.save(room);


        OffsetDateTime start =
                OffsetDateTime.now()
                        .plusDays(1)
                        .withNano(0);

        OffsetDateTime end =
                start.plusHours(2);


        String firstReservation = """
            {
              "title": "First meeting",
              "organizerId": %d,
              "roomId": %d,
              "start": "%s",
              "end": "%s",
              "numberOfParticipants": 10,
              "requiredEquipmentCodes": []
            }
            """.formatted(
                organizer.getId(),
                room.getId(),
                start,
                end
        );


        // Première réservation : elle doit fonctionner
        mockMvc.perform(
                        post("/api/reservations")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(firstReservation)
                )
                .andExpect(
                        status().isCreated()
                );


        // Deuxième réservation sur la même période
        String conflictingReservation = """
            {
              "title": "Conflicting meeting",
              "organizerId": %d,
              "roomId": %d,
              "start": "%s",
              "end": "%s",
              "numberOfParticipants": 10,
              "requiredEquipmentCodes": []
            }
            """.formatted(
                organizer.getId(),
                room.getId(),
                start,
                end
        );


        // WHEN + THEN
        mockMvc.perform(
                        post("/api/reservations")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(conflictingReservation)
                )

                .andExpect(
                        status().isConflict()
                )

                .andExpect(
                        jsonPath("$.code")
                                .value("ROOM_ALREADY_RESERVED")
                )

                .andExpect(
                        jsonPath("$.path")
                                .value("/api/reservations")
                );
    }
    @Test
    void shouldAllowBookingSamePeriodAfterCancellation()
            throws Exception {

        // GIVEN
        Building building = new Building();
        building.setName("Building A");
        building.setNumberOfFloors(5);

        building =
                buildingRepository.save(building);


        Organizer organizer = new Organizer();
        organizer.setName("Alice Martin");
        organizer.setEmail("alice@example.org");
        organizer.setBuilding(building);
        organizer.setFloor(2);

        organizer =
                organizerRepository.save(organizer);


        Room room = new Room();
        room.setName("Orion");
        room.setBuilding(building);
        room.setFloor(2);
        room.setCapacity(30);
        room.setStatus(RoomStatus.AVAILABLE);
        room.setEquipment(new HashSet<>());

        room =
                roomRepository.save(room);


        OffsetDateTime start =
                OffsetDateTime.now()
                        .plusDays(1)
                        .withNano(0);

        OffsetDateTime end =
                start.plusHours(1);


        String firstReservation = """
            {
              "title": "First meeting",
              "organizerId": %d,
              "roomId": %d,
              "start": "%s",
              "end": "%s",
              "numberOfParticipants": 10,
              "requiredEquipmentCodes": []
            }
            """.formatted(
                organizer.getId(),
                room.getId(),
                start,
                end
        );


        // Première réservation
        mockMvc.perform(
                        post("/api/reservations")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(firstReservation)
                )
                .andExpect(
                        status().isCreated()
                );


        Long reservationId =
                reservationRepository
                        .findAll()
                        .get(0)
                        .getId();


        // WHEN : on annule la première réservation
        mockMvc.perform(
                        patch(
                                "/api/reservations/"
                                        + reservationId
                                        + "/cancel"
                        )
                )

                // THEN
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("CANCELLED")
                );


        String secondReservation = """
            {
              "title": "Second meeting",
              "organizerId": %d,
              "roomId": %d,
              "start": "%s",
              "end": "%s",
              "numberOfParticipants": 10,
              "requiredEquipmentCodes": []
            }
            """.formatted(
                organizer.getId(),
                room.getId(),
                start,
                end
        );


        // La même salle et exactement la même période
        // doivent maintenant être disponibles
        mockMvc.perform(
                        post("/api/reservations")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(secondReservation)
                )

                .andExpect(
                        status().isCreated()
                )

                .andExpect(
                        jsonPath("$.status")
                                .value("CONFIRMED")
                )

                .andExpect(
                        jsonPath("$.room.id")
                                .value(room.getId())
                );
    }
    @Test
    void shouldReturnCorrectErrorFormatForInvalidReservation()
            throws Exception {

        // GIVEN
        OffsetDateTime start =
                OffsetDateTime.now()
                        .plusDays(1)
                        .withNano(0);

        OffsetDateTime end =
                start.plusHours(1);

        String invalidRequest = """
            {
              "title": "Invalid meeting",
              "organizerId": 1,
              "roomId": 1,
              "start": "%s",
              "end": "%s",
              "numberOfParticipants": 0,
              "requiredEquipmentCodes": []
            }
            """.formatted(
                start,
                end
        );


        // WHEN + THEN
        mockMvc.perform(
                        post("/api/reservations")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(invalidRequest)
                )

                .andExpect(
                        status().isBadRequest()
                )

                .andExpect(
                        jsonPath("$.code")
                                .value("VALIDATION_ERROR")
                )

                .andExpect(
                        jsonPath("$.message")
                                .exists()
                )

                .andExpect(
                        jsonPath("$.timestamp")
                                .exists()
                )

                .andExpect(
                        jsonPath("$.path")
                                .value("/api/reservations")
                )

                .andExpect(
                        jsonPath("$.details")
                                .exists()
                )

                .andExpect(
                        jsonPath("$.fieldErrors")
                                .exists()
                )

                .andExpect(
                        jsonPath(
                                "$.fieldErrors.numberOfParticipants"
                        )
                                .exists()
                );
    }
    @Test
    void shouldReturnReservationIdInErrorDetails()
            throws Exception {

        // GIVEN
        Long reservationId = 999L;

        // WHEN + THEN
        mockMvc.perform(
                        get(
                                "/api/reservations/"
                                        + reservationId
                        )
                )

                .andExpect(
                        status().isNotFound()
                )

                .andExpect(
                        jsonPath("$.code")
                                .value("RESERVATION_NOT_FOUND")
                )

                .andExpect(
                        jsonPath("$.path")
                                .value("/api/reservations/999")
                )

                .andExpect(
                        jsonPath("$.details.reservationId")
                                .value(999)
                )

                .andExpect(
                        jsonPath("$.fieldErrors")
                                .exists()
                );
    }
}