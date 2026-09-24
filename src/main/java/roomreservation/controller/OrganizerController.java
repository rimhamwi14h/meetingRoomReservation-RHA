package roomreservation.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import roomreservation.request.CreateOrganizerRequest;
import roomreservation.model.Organizer;
import roomreservation.service.OrganizerService;
import java.util.List;
import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/organizers")
public class OrganizerController {
    @Autowired
    private OrganizerService organizerService;
    @PostMapping
    public Organizer createOrganizer(
            @Valid @RequestBody CreateOrganizerRequest request) {

        return organizerService.createOrganizer(request);
    }
    @GetMapping
    public List<Organizer> getAllOrganizers(){
        return organizerService.getAllOrganizers();
    }
    @GetMapping("/{id}")
    public Organizer getOrganizer(@PathVariable Long id){
        return organizerService.getOrganizer(id);
    }
}
