package com.fpt.h2s.controllers;

import com.fpt.h2s.models.annotations.RequiredRoles;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.services.commands.requests.*;
import com.fpt.h2s.services.commands.responses.HouseOwnerDetailAccommodationRoomResponse;
import com.fpt.h2s.services.commands.room.*;
import com.fpt.h2s.utilities.SpringBeans;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/room")
@SuppressWarnings("unused")
public class RoomController {
    @PostMapping("/house-owner/validate")
    @RequiredRoles({User.Role.HOUSE_OWNER})
    @Operation(summary = "Validate room request for house owner")
    public ApiResponse<Void> validateRoomForHouseOwner
            (@Valid @RequestBody final HouseOwnerValidateCreateRoomCommand.Request request) {
        return SpringBeans.getBean(HouseOwnerValidateCreateRoomCommand.class).execute(request);
    }

    @PostMapping("/house-owner/create")
    @RequiredRoles({User.Role.HOUSE_OWNER})
    @Operation(summary = "Update accommodation to create a new room for house owner")
    public ApiResponse<Void> addRoomForHouseOwner
            (@Valid @RequestBody final HouseOwnerCreateRoomRequest request) {
        return SpringBeans.getBean(HouseOwnerCreateRoomCommand.class).execute(request);
    }

    @PostMapping("/house-owner/update/validate")
    @RequiredRoles({User.Role.HOUSE_OWNER})
    @Operation(summary = "Validate room request for house owner")
    public ApiResponse<Void> validateRoomForHouseOwner
            (@Valid @RequestBody final HouseOwnerValidateUpdateRoomCommand.Request request) {
        return SpringBeans.getBean(HouseOwnerValidateUpdateRoomCommand.class).execute(request);
    }

    @PutMapping("/house-owner/update")
    @RequiredRoles({User.Role.HOUSE_OWNER})
    @Operation(summary = "Update accommodation to update room for house owner")
    public ApiResponse<Void> updateRoomForHouseOwner
            (@Valid @RequestBody final HouseOwnerUpdateRoomCommand.Request request) {
        return SpringBeans.getBean(HouseOwnerUpdateRoomCommand.class).execute(request);
    }


    @PostMapping("/house-owner/update/validate/amount")
    @RequiredRoles({User.Role.HOUSE_OWNER})
    @Operation(summary = "Update accommodation to update room amount for house owner")
    public ApiResponse<Void> validUpdateRoomAmountForHouseOwner
            (@Valid @RequestBody final HouseOwnerValidateUpdateRoomPriceCommand.Request request) {
        return SpringBeans.getBean(HouseOwnerValidateUpdateRoomPriceCommand.class).execute(request);
    }

    @PutMapping("/house-owner/update/amount")
    @RequiredRoles({User.Role.HOUSE_OWNER})
    @Operation(summary = "Update accommodation to update room amount for house owner")
    public ApiResponse<Void> updateRoomAmountForHouseOwner
            (@Valid @RequestBody final HouseOwnerUpdateRoomAmountRequest request) {
        return SpringBeans.getBean(HouseOwnerUpdateRoomPriceCommand.class).execute(request);
    }

    @GetMapping("/house-owner/detail")
    @RequiredRoles({User.Role.HOUSE_OWNER})
    @Operation(summary = "View detail room")
    public ApiResponse<HouseOwnerDetailAccommodationRoomResponse> detailForHouseOwner
            (@Valid @ModelAttribute final HouseOwnerViewDetailRoomRequest request) {
        return SpringBeans.getBean(HouseOwnerViewDetailRoomCommand.class).execute(request);
    }

    @PostMapping("/house-owner/upload")
    @Operation(summary = "Upload image for house owner")
    public ApiResponse<String> upload(@RequestPart final MultipartFile image) {
        return SpringBeans.getBean(HouseOwnerCreateRoomUploadImageCommand.class).execute(image);
    }

}
