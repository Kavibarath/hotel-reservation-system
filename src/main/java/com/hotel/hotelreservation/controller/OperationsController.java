package com.hotel.hotelreservation.controller;

import com.hotel.hotelreservation.dto.OperationsDtos.CleaningTaskResponse;
import com.hotel.hotelreservation.dto.OperationsDtos.MaintenanceRequestRequest;
import com.hotel.hotelreservation.dto.OperationsDtos.MaintenanceRequestResponse;
import com.hotel.hotelreservation.model.*;
import com.hotel.hotelreservation.repository.CleaningTaskRepository;
import com.hotel.hotelreservation.repository.MaintenanceRequestRepository;
import com.hotel.hotelreservation.repository.RoomRepository;
import com.hotel.hotelreservation.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Transactional
public class OperationsController {

    private final CleaningTaskRepository cleaningTaskRepository;
    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    @GetMapping("/api/admin/cleaning-tasks")
    @Transactional(readOnly = true)
    public List<CleaningTaskResponse> listCleaningTasks(@RequestParam(required = false) CleaningTaskStatus status) {
        List<CleaningTask> tasks = status == null
                ? cleaningTaskRepository.findAll()
                : cleaningTaskRepository.findByStatus(status);
        return tasks.stream().map(this::toCleaningResponse).toList();
    }

    @PostMapping("/api/admin/cleaning-tasks/{id}/assign")
    public CleaningTaskResponse assignOrCompleteCleaningTask(
            @PathVariable Long id,
            @RequestParam(required = false) Long staffId,
            @RequestParam(defaultValue = "false") boolean complete
    ) {
        CleaningTask task = cleaningTaskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cleaning task not found"));
        if (staffId != null) {
            User staff = userRepository.findById(staffId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Staff user not found"));
            if (staff.getRole() != Role.STAFF && staff.getRole() != Role.ADMIN) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assigned user must be STAFF or ADMIN");
            }
            task.setAssignedStaff(staff);
            task.setStatus(CleaningTaskStatus.IN_PROGRESS);
        }
        if (complete) {
            task.setStatus(CleaningTaskStatus.COMPLETED);
            task.setCompletedAt(Instant.now());
        }
        return toCleaningResponse(cleaningTaskRepository.save(task));
    }

    @PostMapping("/api/maintenance-requests")
    @ResponseStatus(HttpStatus.CREATED)
    public MaintenanceRequestResponse createMaintenanceRequest(@Valid @RequestBody MaintenanceRequestRequest request) {
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        User createdBy = request.createdByUserId() == null ? null : userRepository.findById(request.createdByUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        MaintenanceRequest saved = maintenanceRequestRepository.save(MaintenanceRequest.builder()
                .room(room)
                .description(request.description().trim())
                .createdBy(createdBy)
                .status(MaintenanceRequestStatus.OPEN)
                .build());
        return toMaintenanceResponse(saved);
    }

    @GetMapping("/api/admin/maintenance-requests")
    @Transactional(readOnly = true)
    public List<MaintenanceRequestResponse> listMaintenanceRequests(
            @RequestParam(required = false) MaintenanceRequestStatus status,
            @RequestParam(required = false) Long roomId
    ) {
        List<MaintenanceRequest> requests;
        if (status != null) {
            requests = maintenanceRequestRepository.findByStatus(status);
        } else if (roomId != null) {
            requests = maintenanceRequestRepository.findByRoomId(roomId);
        } else {
            requests = maintenanceRequestRepository.findAll();
        }
        return requests.stream().map(this::toMaintenanceResponse).toList();
    }

    @PostMapping("/api/admin/maintenance-requests/{id}/resolve")
    public MaintenanceRequestResponse resolveMaintenanceRequest(
            @PathVariable Long id,
            @RequestParam(required = false) Long resolvedByUserId
    ) {
        MaintenanceRequest request = maintenanceRequestRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Maintenance request not found"));
        User resolvedBy = resolvedByUserId == null ? null : userRepository.findById(resolvedByUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        request.setStatus(MaintenanceRequestStatus.RESOLVED);
        request.setResolvedBy(resolvedBy);
        request.setResolvedAt(Instant.now());
        return toMaintenanceResponse(maintenanceRequestRepository.save(request));
    }

    private CleaningTaskResponse toCleaningResponse(CleaningTask task) {
        return new CleaningTaskResponse(
                task.getId(),
                task.getRoom().getId(),
                task.getRoom().getRoomNumber(),
                task.getStatus(),
                task.getAssignedStaff() != null ? task.getAssignedStaff().getId() : null,
                task.getAssignedStaff() != null ? task.getAssignedStaff().getFullName() : null,
                task.getCreatedAt(),
                task.getCompletedAt()
        );
    }

    private MaintenanceRequestResponse toMaintenanceResponse(MaintenanceRequest request) {
        return new MaintenanceRequestResponse(
                request.getId(),
                request.getRoom().getId(),
                request.getRoom().getRoomNumber(),
                request.getDescription(),
                request.getStatus(),
                request.getCreatedBy() != null ? request.getCreatedBy().getId() : null,
                request.getResolvedBy() != null ? request.getResolvedBy().getId() : null,
                request.getCreatedAt(),
                request.getResolvedAt()
        );
    }
}
