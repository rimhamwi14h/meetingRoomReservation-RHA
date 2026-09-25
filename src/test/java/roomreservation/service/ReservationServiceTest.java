package roomreservation.service;

import org.junit.jupiter.api.Test;
import roomreservation.model.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import roomreservation.request.AutomaticReservationRequest;
import roomreservation.repository.EquipmentRepository;
import roomreservation.repository.OrganizerRepository;
import roomreservation.repository.ReservationRepository;
import roomreservation.repository.RoomRepository;
import roomreservation.response.ReservationResponse;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import roomreservation.exception.NoCompatibleRoomException;

import static org.junit.jupiter.api.Assertions.assertThrows;


class ReservationServiceTest {
    @ExtendWith(MockitoExtension.class)
    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private OrganizerRepository organizerRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void shouldCalculateDistanceInSameBuilding() {

        // GIVEN
        Building building = new Building();
        building.setId(1L);

        Organizer organizer = new Organizer();
        organizer.setBuilding(building);
        organizer.setFloor(2);

        Room room = new Room();
        room.setBuilding(building);
        room.setFloor(5);

        ReservationService reservationService =
                new ReservationService();

        // WHEN
        int distance =
                reservationService.calculateDistance(
                        room,
                        organizer
                );

        // THEN
        assertEquals(3, distance);
    }
    @Test
    void shouldCalculateDistanceBetweenDifferentBuildings() {

        // GIVEN
        Building organizerBuilding = new Building();
        organizerBuilding.setId(1L);

        Building roomBuilding = new Building();
        roomBuilding.setId(2L);

        Organizer organizer = new Organizer();
        organizer.setBuilding(organizerBuilding);
        organizer.setFloor(2);

        Room room = new Room();
        room.setBuilding(roomBuilding);
        room.setFloor(5);

        ReservationService reservationService =
                new ReservationService();

        // WHEN
        int distance =
                reservationService.calculateDistance(
                        room,
                        organizer
                );

        // THEN
        assertEquals(13, distance);
    }
    @Test
    void shouldCalculateScore() {

        // GIVEN
        Building building = new Building();
        building.setId(1L);

        Organizer organizer = new Organizer();
        organizer.setBuilding(building);
        organizer.setFloor(2);

        Room room = new Room();
        room.setBuilding(building);
        room.setFloor(5);
        room.setCapacity(30);

        ReservationService reservationService =
                new ReservationService();

        // WHEN
        long score =
                reservationService.calculateScore(
                        room,
                        organizer,
                        20
                );

        // THEN
        assertEquals(40, score);
    }
    @Test
    void shouldAcceptRoomWithExactCapacity() {

        // GIVEN
        Building building = new Building();
        building.setId(1L);

        Organizer organizer = new Organizer();
        organizer.setId(1L);
        organizer.setBuilding(building);
        organizer.setFloor(2);

        Room room = new Room();
        room.setId(1L);
        room.setName("Orion");
        room.setBuilding(building);
        room.setFloor(2);
        room.setCapacity(20);
        room.setStatus(RoomStatus.AVAILABLE);
        room.setEquipment(new HashSet<>());

        AutomaticReservationRequest request =
                new AutomaticReservationRequest();

        request.setTitle("Exact capacity meeting");
        request.setOrganizerId(1L);
        request.setStart(
                OffsetDateTime.now().plusDays(1)
        );
        request.setEnd(
                OffsetDateTime.now()
                        .plusDays(1)
                        .plusHours(1)
        );
        request.setNumberOfParticipants(20);
        request.setRequiredEquipmentCodes(
                new HashSet<>()
        );

        when(organizerRepository.findById(1L))
                .thenReturn(Optional.of(organizer));

        when(roomRepository.findAll())
                .thenReturn(List.of(room));

        when(reservationRepository
                .existsByRoomAndStatusAndStartLessThanAndEndGreaterThan(
                        any(),
                        any(),
                        any(),
                        any()
                ))
                .thenReturn(false);

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        // WHEN
        ReservationResponse response =
                reservationService
                        .createAutomaticReservation(request);

        // THEN
        assertEquals(
                1L,
                response.getRoom().getId()
        );
    }
    @Test
    void shouldRejectRoomWithInsufficientCapacity() {

        // GIVEN
        Building building = new Building();
        building.setId(1L);

        Organizer organizer = new Organizer();
        organizer.setId(1L);
        organizer.setBuilding(building);
        organizer.setFloor(2);

        Room room = new Room();
        room.setId(1L);
        room.setName("Small Room");
        room.setBuilding(building);
        room.setFloor(2);
        room.setCapacity(10);
        room.setStatus(RoomStatus.AVAILABLE);
        room.setEquipment(new HashSet<>());

        OffsetDateTime start =
                OffsetDateTime.now().plusDays(1);

        AutomaticReservationRequest request =
                new AutomaticReservationRequest();

        request.setTitle("Too many participants");
        request.setOrganizerId(1L);
        request.setStart(start);
        request.setEnd(start.plusHours(1));
        request.setNumberOfParticipants(20);
        request.setRequiredEquipmentCodes(
                new HashSet<>()
        );

        when(organizerRepository.findById(1L))
                .thenReturn(Optional.of(organizer));

        when(roomRepository.findAll())
                .thenReturn(List.of(room));

        // WHEN + THEN
        assertThrows(
                NoCompatibleRoomException.class,
                () -> reservationService
                        .createAutomaticReservation(request)
        );
    }
    @Test
    void shouldRejectRoomInMaintenance() {

        // GIVEN
        Building building = new Building();
        building.setId(1L);

        Organizer organizer = new Organizer();
        organizer.setId(1L);
        organizer.setBuilding(building);
        organizer.setFloor(2);

        Room room = new Room();
        room.setId(1L);
        room.setName("Maintenance Room");
        room.setBuilding(building);
        room.setFloor(2);
        room.setCapacity(30);
        room.setStatus(RoomStatus.MAINTENANCE);
        room.setEquipment(new HashSet<>());

        OffsetDateTime start =
                OffsetDateTime.now().plusDays(1);

        AutomaticReservationRequest request =
                new AutomaticReservationRequest();

        request.setTitle("Maintenance test");
        request.setOrganizerId(1L);
        request.setStart(start);
        request.setEnd(start.plusHours(1));
        request.setNumberOfParticipants(10);
        request.setRequiredEquipmentCodes(
                new HashSet<>()
        );

        when(organizerRepository.findById(1L))
                .thenReturn(Optional.of(organizer));

        when(roomRepository.findAll())
                .thenReturn(List.of(room));

        // WHEN + THEN
        assertThrows(
                NoCompatibleRoomException.class,
                () -> reservationService
                        .createAutomaticReservation(request)
        );
    }
    @Test
    void shouldRejectAlreadyReservedRoom() {

        // GIVEN
        Building building = new Building();
        building.setId(1L);

        Organizer organizer = new Organizer();
        organizer.setId(1L);
        organizer.setBuilding(building);
        organizer.setFloor(2);

        Room room = new Room();
        room.setId(1L);
        room.setName("Orion");
        room.setBuilding(building);
        room.setFloor(2);
        room.setCapacity(30);
        room.setStatus(RoomStatus.AVAILABLE);
        room.setEquipment(new HashSet<>());

        OffsetDateTime start =
                OffsetDateTime.now().plusDays(1);

        AutomaticReservationRequest request =
                new AutomaticReservationRequest();

        request.setTitle("Conflict test");
        request.setOrganizerId(1L);
        request.setStart(start);
        request.setEnd(start.plusHours(1));
        request.setNumberOfParticipants(10);
        request.setRequiredEquipmentCodes(
                new HashSet<>()
        );

        when(organizerRepository.findById(1L))
                .thenReturn(Optional.of(organizer));

        when(roomRepository.findAll())
                .thenReturn(List.of(room));

        when(reservationRepository
                .existsByRoomAndStatusAndStartLessThanAndEndGreaterThan(
                        any(),
                        any(),
                        any(),
                        any()
                ))
                .thenReturn(true);

        // WHEN + THEN
        assertThrows(
                NoCompatibleRoomException.class,
                () -> reservationService
                        .createAutomaticReservation(request)
        );
    }
    @Test
    void shouldRejectRoomWithoutRequiredEquipment() {

        // GIVEN
        Building building = new Building();
        building.setId(1L);

        Organizer organizer = new Organizer();
        organizer.setId(1L);
        organizer.setBuilding(building);
        organizer.setFloor(2);

        Equipment projector = new Equipment();
        projector.setId(1L);
        projector.setCode("PROJECTOR");
        projector.setLabel("Projector");

        Room room = new Room();
        room.setId(1L);
        room.setName("Orion");
        room.setBuilding(building);
        room.setFloor(2);
        room.setCapacity(30);
        room.setStatus(RoomStatus.AVAILABLE);

        // La salle ne possède aucun équipement
        room.setEquipment(new HashSet<>());

        OffsetDateTime start =
                OffsetDateTime.now().plusDays(1);

        AutomaticReservationRequest request =
                new AutomaticReservationRequest();

        request.setTitle("Equipment test");
        request.setOrganizerId(1L);
        request.setStart(start);
        request.setEnd(start.plusHours(1));
        request.setNumberOfParticipants(10);
        request.setRequiredEquipmentCodes(
                Set.of("PROJECTOR")
        );

        when(organizerRepository.findById(1L))
                .thenReturn(Optional.of(organizer));

        when(equipmentRepository.findByCode("PROJECTOR"))
                .thenReturn(projector);

        when(roomRepository.findAll())
                .thenReturn(List.of(room));

        // WHEN + THEN
        assertThrows(
                NoCompatibleRoomException.class,
                () -> reservationService
                        .createAutomaticReservation(request)
        );
    }
    @Test
    void shouldAcceptConsecutiveReservation() {

        // GIVEN
        Building building = new Building();
        building.setId(1L);

        Organizer organizer = new Organizer();
        organizer.setId(1L);
        organizer.setBuilding(building);
        organizer.setFloor(2);

        Room room = new Room();
        room.setId(1L);
        room.setName("Orion");
        room.setBuilding(building);
        room.setFloor(2);
        room.setCapacity(30);
        room.setStatus(RoomStatus.AVAILABLE);
        room.setEquipment(new HashSet<>());

        OffsetDateTime start =
                OffsetDateTime.now().plusDays(1)
                        .withHour(11)
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0);

        AutomaticReservationRequest request =
                new AutomaticReservationRequest();

        request.setTitle("Consecutive reservation");
        request.setOrganizerId(1L);
        request.setStart(start);
        request.setEnd(start.plusHours(1));
        request.setNumberOfParticipants(10);
        request.setRequiredEquipmentCodes(
                new HashSet<>()
        );

        when(organizerRepository.findById(1L))
                .thenReturn(Optional.of(organizer));

        when(roomRepository.findAll())
                .thenReturn(List.of(room));

        /*
         * On suppose qu'une réservation précédente
         * existe de 10:00 à 11:00.
         *
         * La nouvelle commence à 11:00,
         * donc il n'y a PAS de chevauchement.
         */
        when(reservationRepository
                .existsByRoomAndStatusAndStartLessThanAndEndGreaterThan(
                        any(),
                        any(),
                        any(),
                        any()
                ))
                .thenReturn(false);

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        // WHEN
        ReservationResponse response =
                reservationService
                        .createAutomaticReservation(request);

        // THEN
        assertEquals(
                1L,
                response.getRoom().getId()
        );
    }
    @Test
    void shouldSelectRoomWithLowestScore() {

        // GIVEN
        Building building = new Building();
        building.setId(1L);

        Organizer organizer = new Organizer();
        organizer.setId(1L);
        organizer.setBuilding(building);
        organizer.setFloor(2);

        Room nearRoom = new Room();
        nearRoom.setId(1L);
        nearRoom.setName("Near Room");
        nearRoom.setBuilding(building);
        nearRoom.setFloor(2);
        nearRoom.setCapacity(25);
        nearRoom.setStatus(RoomStatus.AVAILABLE);
        nearRoom.setEquipment(new HashSet<>());

        Room farRoom = new Room();
        farRoom.setId(2L);
        farRoom.setName("Far Room");
        farRoom.setBuilding(building);
        farRoom.setFloor(4);
        farRoom.setCapacity(20);
        farRoom.setStatus(RoomStatus.AVAILABLE);
        farRoom.setEquipment(new HashSet<>());

        OffsetDateTime start =
                OffsetDateTime.now().plusDays(1);

        AutomaticReservationRequest request =
                new AutomaticReservationRequest();

        request.setTitle("Best room test");
        request.setOrganizerId(1L);
        request.setStart(start);
        request.setEnd(start.plusHours(1));
        request.setNumberOfParticipants(20);
        request.setRequiredEquipmentCodes(
                new HashSet<>()
        );

        when(organizerRepository.findById(1L))
                .thenReturn(Optional.of(organizer));

        when(roomRepository.findAll())
                .thenReturn(
                        List.of(farRoom, nearRoom)
                );

        when(reservationRepository
                .existsByRoomAndStatusAndStartLessThanAndEndGreaterThan(
                        any(),
                        any(),
                        any(),
                        any()
                ))
                .thenReturn(false);

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        // WHEN
        ReservationResponse response =
                reservationService
                        .createAutomaticReservation(request);

        // THEN
        assertEquals(
                1L,
                response.getRoom().getId()
        );

        assertEquals(
                "Near Room",
                response.getRoom().getName()
        );
    }
    @Test
    void shouldSelectRoomAlphabeticallyWhenScoresAreEqual() {

        // GIVEN
        Building building = new Building();
        building.setId(1L);

        Organizer organizer = new Organizer();
        organizer.setId(1L);
        organizer.setBuilding(building);
        organizer.setFloor(2);

        Room betaRoom = new Room();
        betaRoom.setId(1L);
        betaRoom.setName("Beta");
        betaRoom.setBuilding(building);
        betaRoom.setFloor(2);
        betaRoom.setCapacity(30);
        betaRoom.setStatus(RoomStatus.AVAILABLE);
        betaRoom.setEquipment(new HashSet<>());

        Room alphaRoom = new Room();
        alphaRoom.setId(2L);
        alphaRoom.setName("Alpha");
        alphaRoom.setBuilding(building);
        alphaRoom.setFloor(2);
        alphaRoom.setCapacity(30);
        alphaRoom.setStatus(RoomStatus.AVAILABLE);
        alphaRoom.setEquipment(new HashSet<>());

        OffsetDateTime start =
                OffsetDateTime.now().plusDays(1);

        AutomaticReservationRequest request =
                new AutomaticReservationRequest();

        request.setTitle("Alphabetical test");
        request.setOrganizerId(1L);
        request.setStart(start);
        request.setEnd(start.plusHours(1));
        request.setNumberOfParticipants(20);
        request.setRequiredEquipmentCodes(
                new HashSet<>()
        );

        when(organizerRepository.findById(1L))
                .thenReturn(Optional.of(organizer));

        // Beta est volontairement placé avant Alpha
        when(roomRepository.findAll())
                .thenReturn(
                        List.of(betaRoom, alphaRoom)
                );

        when(reservationRepository
                .existsByRoomAndStatusAndStartLessThanAndEndGreaterThan(
                        any(),
                        any(),
                        any(),
                        any()
                ))
                .thenReturn(false);

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        // WHEN
        ReservationResponse response =
                reservationService
                        .createAutomaticReservation(request);

        // THEN
        assertEquals(
                "Alpha",
                response.getRoom().getName()
        );

        assertEquals(
                2L,
                response.getRoom().getId()
        );
    }
    @Test
    void shouldSelectLowestIdWhenNamesAndScoresAreEqual() {

        // GIVEN
        Building building = new Building();
        building.setId(1L);

        Organizer organizer = new Organizer();
        organizer.setId(1L);
        organizer.setBuilding(building);
        organizer.setFloor(2);

        Room roomWithHigherId = new Room();
        roomWithHigherId.setId(2L);
        roomWithHigherId.setName("Orion");
        roomWithHigherId.setBuilding(building);
        roomWithHigherId.setFloor(2);
        roomWithHigherId.setCapacity(30);
        roomWithHigherId.setStatus(RoomStatus.AVAILABLE);
        roomWithHigherId.setEquipment(new HashSet<>());

        Room roomWithLowerId = new Room();
        roomWithLowerId.setId(1L);
        roomWithLowerId.setName("Orion");
        roomWithLowerId.setBuilding(building);
        roomWithLowerId.setFloor(2);
        roomWithLowerId.setCapacity(30);
        roomWithLowerId.setStatus(RoomStatus.AVAILABLE);
        roomWithLowerId.setEquipment(new HashSet<>());

        OffsetDateTime start =
                OffsetDateTime.now().plusDays(1);

        AutomaticReservationRequest request =
                new AutomaticReservationRequest();

        request.setTitle("Id tie break test");
        request.setOrganizerId(1L);
        request.setStart(start);
        request.setEnd(start.plusHours(1));
        request.setNumberOfParticipants(20);
        request.setRequiredEquipmentCodes(
                new HashSet<>()
        );

        when(organizerRepository.findById(1L))
                .thenReturn(Optional.of(organizer));

        // On met volontairement l'id 2 avant l'id 1.
        when(roomRepository.findAll())
                .thenReturn(
                        List.of(
                                roomWithHigherId,
                                roomWithLowerId
                        )
                );

        when(reservationRepository
                .existsByRoomAndStatusAndStartLessThanAndEndGreaterThan(
                        any(),
                        any(),
                        any(),
                        any()
                ))
                .thenReturn(false);

        when(reservationRepository.save(any(Reservation.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        // WHEN
        ReservationResponse response =
                reservationService
                        .createAutomaticReservation(request);

        // THEN
        assertEquals(
                1L,
                response.getRoom().getId()
        );
    }
    @Test
    void shouldThrowExceptionWhenNoCompatibleRoomExists() {

        // GIVEN
        Building building = new Building();
        building.setId(1L);

        Organizer organizer = new Organizer();
        organizer.setId(1L);
        organizer.setBuilding(building);
        organizer.setFloor(2);

        OffsetDateTime start =
                OffsetDateTime.now().plusDays(1);

        AutomaticReservationRequest request =
                new AutomaticReservationRequest();

        request.setTitle("No room test");
        request.setOrganizerId(1L);
        request.setStart(start);
        request.setEnd(start.plusHours(1));
        request.setNumberOfParticipants(10);
        request.setRequiredEquipmentCodes(
                new HashSet<>()
        );

        when(organizerRepository.findById(1L))
                .thenReturn(Optional.of(organizer));

        // Aucune salle dans le système
        when(roomRepository.findAll())
                .thenReturn(List.of());

        // WHEN + THEN
        assertThrows(
                NoCompatibleRoomException.class,
                () -> reservationService
                        .createAutomaticReservation(request)
        );
    }
}