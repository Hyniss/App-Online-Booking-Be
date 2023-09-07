package com.fpt.h2s.models.entities;

import ananta.utility.StreamEx;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fpt.h2s.models.entities.converters.JPAEnumConverter;
import com.fpt.h2s.models.entities.listeners.CreatorIdListener;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@With
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@ToString
/* Jpa */
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "accommodations")
@EntityListeners({CreatorIdListener.class})
@BatchSize(size = 50)
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    private Integer ownerId;

    private String thumbnail;

    private String shortDescription;

    private String description;

    private String address;

    private Double latitude;

    private Double longitude;

    private Long minPrice;

    private Long maxPrice;

    @Enumerated(EnumType.STRING)
    private Type type;

    @Enumerated(EnumType.STRING)
    private Status status;

    private Integer totalRoom;

    private Integer totalViews;

    private Integer totalLikes;

    private Integer totalReviews;

    private Integer totalBookings;

    private Float reviewRate;

    private Integer creatorId;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    private Timestamp deletedAt;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = AccommodationReview.Fields.accommodationId, insertable = false, updatable = false)
    private Set<AccommodationReview> reviews;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = AccommodationImage.Fields.accommodationId, insertable = false, updatable = false)
    private Set<AccommodationImage> images;

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
    @JoinColumn(name = Room.Fields.accommodationId, insertable = false, updatable = false)
    private Set<Room> rooms;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = Fields.ownerId, insertable = false, updatable = false)
    private User owner;

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = Contract.Fields.id, insertable = false, updatable = false)
    private List<Contract> contracts;

    public boolean isCreatedBy(User user) {
        return getCreatorId().equals(user.getId());
    }

    public Contract getContract() {
        return StreamEx.from(this.getContracts()).findFirst().orElse(null);
    }

    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany
    @JoinTable(name = "accommodations_categories",
        joinColumns = @JoinColumn(name = "accommodation_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories;

    public enum Type {
        HOUSE,
        APARTMENT,
        GUEST_HOUSE,
        HOTEL;

        @Component
        public static class Converter extends JPAEnumConverter<Accommodation.Type> {
        }
    }

    public enum Status {
        PENDING, REJECTED, OPENING, CLOSED, BANNED;

        @Component
        public static class Converter extends JPAEnumConverter<Accommodation.Status> {
        }
    }

    public boolean is(final Accommodation.Type type) {
        return this.type == type;
    }

}
