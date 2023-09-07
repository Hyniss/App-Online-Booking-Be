package com.fpt.h2s.controllers;

import com.fpt.h2s.models.annotations.RequiredRoles;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.services.commands.contract.AdminChangeStatusContractCommand;
import com.fpt.h2s.services.commands.contract.AdminSearchContractCommand;
import com.fpt.h2s.services.commands.responses.AdminContractResponse;
import com.fpt.h2s.utilities.SpringBeans;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/contract")
@SuppressWarnings("unused")
public class ContractController {
    @PostMapping("/admin")
    @RequiredRoles({User.Role.ADMIN})
    @Operation(summary = "View contracts for admin")
    public static ApiResponse<ListResult<AdminContractResponse>> viewContractsForAdmin(@Valid @RequestBody final AdminSearchContractCommand.AdminSearchContractCommandRequest request) {
        return SpringBeans.getBean(AdminSearchContractCommand.class).execute(request);
    }

    @PutMapping("/admin/change-status")
    @RequiredRoles({User.Role.ADMIN})
    @Operation(summary = "Change status of contract")
    public static ApiResponse<Void> changeStatusContract(@Valid @ModelAttribute final AdminChangeStatusContractCommand.AdminChangeStatusContractRequest request) {
        return SpringBeans.getBean(AdminChangeStatusContractCommand.class).execute(request);
    }

}
