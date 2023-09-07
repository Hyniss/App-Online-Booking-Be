package com.fpt.h2s.models.entities;

import ananta.utility.StreamEx;
import ananta.utility.StringEx;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fpt.h2s.models.entities.converters.JPAEnumConverter;
import com.fpt.h2s.models.entities.listeners.CreatorIdListener;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.RedisRepository;
import com.fpt.h2s.utilities.MoreRequests;
import com.fpt.h2s.utilities.Tokens;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.*;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;

@With
@Getter
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@FieldNameConstants
@ToString
/* Jpa */
@Entity
@DynamicInsert
@DynamicUpdate
@EntityListeners({CreatorIdListener.class})
@BatchSize(size = 50)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    private String email;
    
    private String username;
    
    private String phone;
    
    private String password;
    
    private String roles;
    
    @Convert(converter = Status.Converter.class)
    private Status status;
    
    private Integer companyId;
    private Integer creatorId;
    
    @CreationTimestamp
    private Timestamp createdAt;
    
    @UpdateTimestamp
    private Timestamp updatedAt;
    
    private Timestamp deletedAt;
    
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.DETACH})
    @JoinColumn(name = UserProfile.Fields.userId, insertable = false, updatable = false)
    private List<UserProfile> userProfiles;
    
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = Fields.companyId, insertable = false, updatable = false)
    private Company company;
    
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = BookingRequest.Fields.userId, insertable = false, updatable = false)
    private Set<BookingRequest> bookingRequests;
    
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = Room.Fields.creatorId, insertable = false, updatable = false)
    private Set<Room> rooms;
    
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = Accommodation.Fields.creatorId, insertable = false, updatable = false)
    private Set<Accommodation> accommodations;
    
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = Transaction.Fields.creatorId, insertable = false, updatable = false)
    private Set<Transaction> transactions;
    
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = AccommodationReview.Fields.userId, insertable = false, updatable = false)
    private Set<AccommodationReview> reviews;
    
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = Like.PK.Fields.userId, insertable = false, updatable = false)
    private Set<Like> likedAccommodations;
    
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = Contract.Fields.id, insertable = false, updatable = false)
    private Set<Contract> contract;
    
    public final UserProfile getUserProfile() {
        return StreamEx.from(this.getUserProfiles()).findFirst().orElse(null);
    }

    public List<Role> roleList() {
        if(roles == null) return Collections.emptyList();
        return Arrays.stream(this.roles.split(", ")).filter(StringEx::isNotBlank).map(Role::valueOf).toList();
    }
    
    public boolean is(final Status status) {
        return this.status == status;
    }
    
    public boolean is(final Role role) {
        return this.roleList().contains(role);
    }
    
    public boolean isBusinessUserOneOf() {
        return this.isOneOf(Role.BUSINESS_MEMBER, Role.BUSINESS_OWNER, Role.BUSINESS_ADMIN);
    }
    
    public boolean isOneOf(final Role... roles) {
        return Arrays.stream(roles).anyMatch(this::is);
    }

    public boolean isOneOf(final Status... statuses) {
        return Arrays.stream(statuses).anyMatch(this::is);
    }

    public static Optional<Integer> currentUserId() {
        return Tokens
            .findTokenFrom(MoreRequests.getCurrentHttpRequest())
            .flatMap(token -> RedisRepository.get(token, User.class))
            .map(User::getId);
    }

    public static Integer getCurrentId() {
        return Tokens
            .findTokenFrom(MoreRequests.getCurrentHttpRequest())
            .flatMap(token -> RedisRepository.get(token, User.class))
            .map(User::getId)
            .orElseThrow(ApiException::unauthorized);
    }


    public static final User DUMMY = User.builder().id(-1).build();

    public enum Role {
        ADMIN, CUSTOMER, HOUSE_OWNER, BUSINESS_OWNER, BUSINESS_ADMIN, BUSINESS_MEMBER
    }
    
    public enum Status {
        PENDING, ACTIVE, BANNED;
        
        @Component
        public static class Converter extends JPAEnumConverter<Status> {
        }
        
    }
}
