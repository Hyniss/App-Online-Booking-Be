package com.fpt.h2s.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fpt.h2s.models.entities.listeners.CreatorIdListener;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;

import java.sql.Timestamp;
import java.util.Set;


@With
@Getter
@Setter(AccessLevel.PRIVATE)
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@ToString
/* Jpa */
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "rooms")
@EntityListeners({CreatorIdListener.class})
@BatchSize(size = 50)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer accommodationId;

    private String name;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Long price;

    @Version
    private Integer version;

    @Column(name = "count")
    private Integer totalRooms;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = Fields.creatorId, insertable = false, updatable = false)
    private User owner;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = RoomProperty.PK.Fields.roomId, insertable = false, updatable = false)
    private Set<RoomProperty> properties;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = RoomImage.Fields.roomId, insertable = false, updatable = false)
    private Set<RoomImage> images;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = Fields.accommodationId, insertable = false, updatable = false)
    private Accommodation accommodation;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany
    @JoinTable(name = "room_amenities",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> amenities;

    private Integer creatorId;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    private Timestamp deletedAt;

    public enum Status {
        OPENING, CLOSED
    }
}
