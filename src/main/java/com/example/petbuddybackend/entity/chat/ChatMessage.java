package com.example.petbuddybackend.entity.chat;

import com.example.petbuddybackend.entity.user.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Builder
@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chatRoomId", referencedColumnName = "id")
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "senderEmail", referencedColumnName = "email")
    private AppUser sender;

    @PrePersist
    public void prePersist() {
        createdAt = ZonedDateTime.now();
    }
}