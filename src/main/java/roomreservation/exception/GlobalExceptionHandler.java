package roomreservation.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import roomreservation.response.ApiErrorResponse;

import java.time.OffsetDateTime;


@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(RoomNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleRoomNotFound(
            RoomNotFoundException exception,
            HttpServletRequest request) {

        ApiErrorResponse error =
                new ApiErrorResponse();

        error.setCode("ROOM_NOT_FOUND");
        error.setMessage(exception.getMessage());
        error.setTimestamp(OffsetDateTime.now());
        error.setPath(request.getRequestURI());

        error.getDetails().put(
                "roomId",
                exception.getRoomId()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }


    @ExceptionHandler(BuildingNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleBuildingNotFound(
            BuildingNotFoundException exception,
            HttpServletRequest request) {

        ApiErrorResponse error =
                new ApiErrorResponse();

        error.setCode("BUILDING_NOT_FOUND");
        error.setMessage(exception.getMessage());
        error.setTimestamp(OffsetDateTime.now());
        error.setPath(request.getRequestURI());

        error.getDetails().put(
                "buildingId",
                exception.getBuildingId()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }


    @ExceptionHandler(OrganizerNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleOrganizerNotFound(
            OrganizerNotFoundException exception,
            HttpServletRequest request) {

        ApiErrorResponse error =
                new ApiErrorResponse();

        error.setCode("ORGANIZER_NOT_FOUND");
        error.setMessage(exception.getMessage());
        error.setTimestamp(OffsetDateTime.now());
        error.setPath(request.getRequestURI());

        error.getDetails().put(
                "organizerId",
                exception.getOrganizerId()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }


    @ExceptionHandler(EquipmentNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleEquipmentNotFound(
            EquipmentNotFoundException exception,
            HttpServletRequest request) {

        ApiErrorResponse error =
                new ApiErrorResponse();

        error.setCode("EQUIPMENT_NOT_FOUND");
        error.setMessage(exception.getMessage());
        error.setTimestamp(OffsetDateTime.now());
        error.setPath(request.getRequestURI());

        error.getDetails().put(
                "equipmentCode",
                exception.getEquipmentCode()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }


    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleReservationNotFound(
            ReservationNotFoundException exception,
            HttpServletRequest request) {

        ApiErrorResponse error =
                new ApiErrorResponse();

        error.setCode("RESERVATION_NOT_FOUND");
        error.setMessage(exception.getMessage());
        error.setTimestamp(OffsetDateTime.now());
        error.setPath(request.getRequestURI());

        error.getDetails().put(
                "reservationId",
                exception.getReservationId()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }


    @ExceptionHandler(RoomUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleRoomUnavailable(
            RoomUnavailableException exception,
            HttpServletRequest request) {

        ApiErrorResponse error =
                new ApiErrorResponse();

        error.setCode("ROOM_UNAVAILABLE");
        error.setMessage(exception.getMessage());
        error.setTimestamp(OffsetDateTime.now());
        error.setPath(request.getRequestURI());

        error.getDetails().put(
                "roomId",
                exception.getRoomId()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }


    @ExceptionHandler(RoomCapacityExceededException.class)
    public ResponseEntity<ApiErrorResponse> handleRoomCapacityExceeded(
            RoomCapacityExceededException exception,
            HttpServletRequest request) {

        ApiErrorResponse error =
                new ApiErrorResponse();

        error.setCode(
                "ROOM_CAPACITY_EXCEEDED"
        );

        error.setMessage(
                exception.getMessage()
        );

        error.setTimestamp(
                OffsetDateTime.now()
        );

        error.setPath(
                request.getRequestURI()
        );

        error.getDetails().put(
                "roomId",
                exception.getRoomId()
        );

        error.getDetails().put(
                "roomCapacity",
                exception.getRoomCapacity()
        );

        error.getDetails().put(
                "numberOfParticipants",
                exception.getNumberOfParticipants()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }


    @ExceptionHandler(MissingRequiredEquipmentException.class)
    public ResponseEntity<ApiErrorResponse> handleMissingRequiredEquipment(
            MissingRequiredEquipmentException exception,
            HttpServletRequest request) {

        ApiErrorResponse error =
                new ApiErrorResponse();

        error.setCode(
                "MISSING_REQUIRED_EQUIPMENT"
        );

        error.setMessage(
                exception.getMessage()
        );

        error.setTimestamp(
                OffsetDateTime.now()
        );

        error.setPath(
                request.getRequestURI()
        );

        error.getDetails().put(
                "roomId",
                exception.getRoomId()
        );

        error.getDetails().put(
                "missingEquipmentCodes",
                exception.getMissingEquipmentCodes()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }


    @ExceptionHandler(RoomAlreadyReservedException.class)
    public ResponseEntity<ApiErrorResponse> handleRoomAlreadyReserved(
            RoomAlreadyReservedException exception,
            HttpServletRequest request) {

        ApiErrorResponse error =
                new ApiErrorResponse();

        error.setCode(
                "ROOM_ALREADY_RESERVED"
        );

        error.setMessage(
                exception.getMessage()
        );

        error.setTimestamp(
                OffsetDateTime.now()
        );

        error.setPath(
                request.getRequestURI()
        );

        error.getDetails().put(
                "roomId",
                exception.getRoomId()
        );

        error.getDetails().put(
                "conflictingReservationId",
                exception.getConflictingReservationId()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }


    @ExceptionHandler(ReservationAlreadyCancelledException.class)
    public ResponseEntity<ApiErrorResponse> handleReservationAlreadyCancelled(
            ReservationAlreadyCancelledException exception,
            HttpServletRequest request) {

        ApiErrorResponse error =
                new ApiErrorResponse();

        error.setCode(
                "RESERVATION_ALREADY_CANCELLED"
        );

        error.setMessage(
                exception.getMessage()
        );

        error.setTimestamp(
                OffsetDateTime.now()
        );

        error.setPath(
                request.getRequestURI()
        );

        error.getDetails().put(
                "reservationId",
                exception.getReservationId()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }


    @ExceptionHandler(NoCompatibleRoomException.class)
    public ResponseEntity<ApiErrorResponse> handleNoCompatibleRoom(
            NoCompatibleRoomException exception,
            HttpServletRequest request) {

        ApiErrorResponse error =
                new ApiErrorResponse();

        error.setCode("NO_COMPATIBLE_ROOM");
        error.setMessage(exception.getMessage());
        error.setTimestamp(OffsetDateTime.now());
        error.setPath(request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }


    @ExceptionHandler(InvalidReservationPeriodException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidReservationPeriod(
            InvalidReservationPeriodException exception,
            HttpServletRequest request) {

        ApiErrorResponse error =
                new ApiErrorResponse();

        error.setCode(
                "INVALID_RESERVATION_PERIOD"
        );

        error.setMessage(
                exception.getMessage()
        );

        error.setTimestamp(
                OffsetDateTime.now()
        );

        error.setPath(
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationError(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        ApiErrorResponse error =
                new ApiErrorResponse();

        error.setCode("VALIDATION_ERROR");

        error.setMessage(
                "The request contains invalid data"
        );

        error.setTimestamp(
                OffsetDateTime.now()
        );

        error.setPath(
                request.getRequestURI()
        );


        for (FieldError fieldError :
                exception.getBindingResult()
                        .getFieldErrors()) {

            error.getFieldErrors().put(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            );
        }


        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }


    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceAlreadyExists(
            ResourceAlreadyExistsException exception,
            HttpServletRequest request) {

        ApiErrorResponse error =
                new ApiErrorResponse();

        error.setCode(
                "RESOURCE_ALREADY_EXISTS"
        );

        error.setMessage(
                exception.getMessage()
        );

        error.setTimestamp(
                OffsetDateTime.now()
        );

        error.setPath(
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }


    @ExceptionHandler(BuildingFloorCountConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleBuildingFloorCountConflict(
            BuildingFloorCountConflictException exception,
            HttpServletRequest request) {

        ApiErrorResponse error =
                new ApiErrorResponse();

        error.setCode(
                "BUILDING_FLOOR_COUNT_CONFLICT"
        );

        error.setMessage(
                exception.getMessage()
        );

        error.setTimestamp(
                OffsetDateTime.now()
        );

        error.setPath(
                request.getRequestURI()
        );

        error.getDetails().put(
                "highestOccupiedFloor",
                exception.getHighestOccupiedFloor()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }


    @ExceptionHandler(InvalidFloorException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidFloor(
            InvalidFloorException exception,
            HttpServletRequest request) {

        ApiErrorResponse error =
                new ApiErrorResponse();

        error.setCode("VALIDATION_ERROR");
        error.setMessage(exception.getMessage());
        error.setTimestamp(OffsetDateTime.now());
        error.setPath(request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request) {

        ApiErrorResponse error =
                new ApiErrorResponse();

        error.setCode("VALIDATION_ERROR");

        error.setMessage(
                "The request contains invalid data"
        );

        error.setTimestamp(
                OffsetDateTime.now()
        );

        error.setPath(
                request.getRequestURI()
        );


        for (ConstraintViolation<?> violation :
                exception.getConstraintViolations()) {

            String propertyPath =
                    violation.getPropertyPath()
                            .toString();

            String field =
                    propertyPath.substring(
                            propertyPath
                                    .lastIndexOf('.')
                                    + 1
                    );

            error.getFieldErrors().put(
                    field,
                    violation.getMessage()
            );
        }


        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(error);
    }
}