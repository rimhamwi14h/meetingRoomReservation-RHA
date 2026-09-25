package roomreservation.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import roomreservation.model.Building;
import roomreservation.model.Room;
import roomreservation.model.RoomStatus;

import roomreservation.repository.BuildingRepository;
import roomreservation.repository.OrganizerRepository;
import roomreservation.repository.ReservationRepository;
import roomreservation.repository.RoomRepository;

import java.time.OffsetDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class RoomIntegrationTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private OrganizerRepository organizerRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private RoomRepository roomRepository;


    @BeforeEach
    void setUp() {

        reservationRepository.deleteAll();
        organizerRepository.deleteAll();
        roomRepository.deleteAll();
        buildingRepository.deleteAll();
    }


    @Test
    void shouldCreateAndGetRoom() throws Exception {

        // GIVEN
        Building building = new Building();
        building.setName("Building A");
        building.setNumberOfFloors(5);

        building =
                buildingRepository.save(building);


        String requestBody = """
                {
                  "name": "Orion",
                  "buildingId": %d,
                  "floor": 2,
                  "capacity": 30,
                  "equipmentCodes": []
                }
                """.formatted(building.getId());


        // WHEN : création de la salle
        MvcResult result =
                mockMvc.perform(
                                post("/api/rooms")
                                        .contentType(
                                                MediaType.APPLICATION_JSON
                                        )
                                        .content(requestBody)
                        )

                        // THEN
                        .andExpect(
                                status().isCreated()
                        )
                        .andExpect(
                                header().exists("Location")
                        )
                        .andExpect(
                                jsonPath("$.name")
                                        .value("Orion")
                        )
                        .andExpect(
                                jsonPath("$.floor")
                                        .value(2)
                        )
                        .andExpect(
                                jsonPath("$.capacity")
                                        .value(30)
                        )
                        .andExpect(
                                jsonPath("$.status")
                                        .value("AVAILABLE")
                        )
                        .andReturn();


        String location =
                result.getResponse()
                        .getHeader("Location");

        assertNotNull(location);


        // WHEN : consultation de la salle créée
        mockMvc.perform(
                        get(location)
                )

                // THEN
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$.name")
                                .value("Orion")
                )
                .andExpect(
                        jsonPath("$.building.id")
                                .value(building.getId())
                )
                .andExpect(
                        jsonPath("$.building.name")
                                .value("Building A")
                )
                .andExpect(
                        jsonPath("$.floor")
                                .value(2)
                )
                .andExpect(
                        jsonPath("$.capacity")
                                .value(30)
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("AVAILABLE")
                );
    }


    @Test
    void shouldFindAvailableRoomsInCorrectOrder()
            throws Exception {

        // GIVEN
        Building building = new Building();
        building.setName("Building A");
        building.setNumberOfFloors(5);

        building =
                buildingRepository.save(building);


        Room largeRoom = new Room();
        largeRoom.setName("Large");
        largeRoom.setBuilding(building);
        largeRoom.setFloor(1);
        largeRoom.setCapacity(40);
        largeRoom.setStatus(
                RoomStatus.AVAILABLE
        );
        largeRoom.setEquipment(
                new HashSet<>()
        );

        roomRepository.save(largeRoom);


        Room smallRoom = new Room();
        smallRoom.setName("Small");
        smallRoom.setBuilding(building);
        smallRoom.setFloor(1);
        smallRoom.setCapacity(25);
        smallRoom.setStatus(
                RoomStatus.AVAILABLE
        );
        smallRoom.setEquipment(
                new HashSet<>()
        );

        roomRepository.save(smallRoom);


        Room mediumRoom = new Room();
        mediumRoom.setName("Medium");
        mediumRoom.setBuilding(building);
        mediumRoom.setFloor(1);
        mediumRoom.setCapacity(30);
        mediumRoom.setStatus(
                RoomStatus.AVAILABLE
        );
        mediumRoom.setEquipment(
                new HashSet<>()
        );

        roomRepository.save(mediumRoom);


        OffsetDateTime start =
                OffsetDateTime.now()
                        .plusDays(1)
                        .withNano(0);

        OffsetDateTime end =
                start.plusHours(1);


        // WHEN + THEN
        mockMvc.perform(
                        get("/api/rooms/available")
                                .param(
                                        "start",
                                        start.toString()
                                )
                                .param(
                                        "end",
                                        end.toString()
                                )
                                .param(
                                        "capacity",
                                        "20"
                                )
                )

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.length()")
                                .value(3)
                )

                // Small : 25 - 20 = 5
                .andExpect(
                        jsonPath("$[0].name")
                                .value("Small")
                )
                .andExpect(
                        jsonPath(
                                "$[0].unusedCapacity"
                        )
                                .value(5)
                )

                // Medium : 30 - 20 = 10
                .andExpect(
                        jsonPath("$[1].name")
                                .value("Medium")
                )
                .andExpect(
                        jsonPath(
                                "$[1].unusedCapacity"
                        )
                                .value(10)
                )

                // Large : 40 - 20 = 20
                .andExpect(
                        jsonPath("$[2].name")
                                .value("Large")
                )
                .andExpect(
                        jsonPath(
                                "$[2].unusedCapacity"
                        )
                                .value(20)
                );
    }
}