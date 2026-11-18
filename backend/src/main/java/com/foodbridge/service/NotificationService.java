package com.foodbridge.service;

import com.foodbridge.dto.ApiDtos.NotificationDto;
import com.foodbridge.entity.NotificationEntity;
import com.foodbridge.entity.UserEntity;
import com.foodbridge.repository.NotificationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notifications;
    private final CurrentUserService currentUser;
    public NotificationService(NotificationRepository notifications, CurrentUserService currentUser) {
        this.notifications = notifications; this.currentUser = currentUser;
    }
    public void notify(UserEntity user, String title, String message, String type) {
        NotificationEntity n = new NotificationEntity();
        n.setUser(user); n.setTitle(title); n.setMessage(message); n.setType(type); notifications.save(n);
    }
    public List<NotificationDto> mine(String email) {
        UserEntity user = currentUser.require(email);
        return notifications.findTop50ByUser_IdOrderByCreatedAtDesc(user.getId()).stream()
                .map(n -> new NotificationDto(n.getId(), n.getTitle(), n.getMessage(), n.getType(), n.isRead(), n.getCreatedAt())).toList();
    }
    public void markRead(String email, Long id) {
        UserEntity user = currentUser.require(email);
        NotificationEntity n = notifications.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        if (!n.getUser().getId().equals(user.getId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your notification");
        n.setRead(true); notifications.save(n);
    }
    @Transactional
    public void markAllRead(String email) {
        UserEntity user = currentUser.require(email);
        notifications.findTop50ByUser_IdOrderByCreatedAtDesc(user.getId()).forEach(n -> n.setRead(true));
    }
}
