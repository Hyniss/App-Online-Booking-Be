package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.models.entities.Contract;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.utilities.Mappers;
import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.sql.Timestamp;
import java.util.List;

@Builder
@Getter
public class AdminContractResponse {
    private Integer id;

    private String name;

    private String content;

    private Integer profit;

    private Contract.Status status;

    private String acceptedName;

    private Timestamp createdAt;

    private Timestamp expiredAt;

    private UserResponse houseOwner;

    public static AdminContractResponse of(Contract contract) {
        return AdminContractResponse
                .builder()
                .id(contract.getId())
                .name(contract.getName())
                .content(contract.getContent())
                .profit(contract.getProfit())
                .status(contract.getStatus())
                .acceptedName((contract.getUser() == null) ? "" : contract.getUser().getUsername())
                .createdAt(contract.getCreatedAt())
                .expiredAt(contract.getExpiredAt())
                .houseOwner(AdminContractResponse.UserResponse.of(contract.getHouseOwner()))
                .build();
    }

    @Builder
    @Getter
    @With
    public static class UserResponse {
        private Integer id;

        private String email;

        private String username;

        private String phone;

        private List<User.Role> roles;

        private User.Status status;

        private Timestamp createdAt;

        private Timestamp updatedAt;

        public static AdminContractResponse.UserResponse of(final User user) {
            return Mappers.convertTo(AdminContractResponse.UserResponse.class, user).withRoles(user.roleList());
        }

    }

}
