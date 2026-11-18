package com.foodbridge.controller;

import com.foodbridge.dto.ApiDtos.*;
import com.foodbridge.service.NotificationService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationService service;
    public NotificationController(NotificationService service) { this.service = service; }
    @GetMapping public List<NotificationDto> mine(Authentication auth) { return service.mine(auth.getName()); }
    @PutMapping("/{id}/read") public MessageResponse read(Authentication auth, @PathVariable Long id) { service.markRead(auth.getName(), id); return new MessageResponse("Notification marked as read"); }
    @PutMapping("/read-all") public MessageResponse readAll(Authentication auth) { service.markAllRead(auth.getName()); return new MessageResponse("Notifications marked as read"); }
}
