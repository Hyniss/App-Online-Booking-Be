package com.fpt.h2s.controllers;

import com.fpt.h2s.models.annotations.RequiredRoles;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.services.commands.responses.BusinessAdminTransactionResponse;
import com.fpt.h2s.services.commands.responses.TransactionResponse;
import com.fpt.h2s.services.commands.transactions.BusinessAdminSearchTransactionCommand;
import com.fpt.h2s.services.commands.transactions.ViewTransactionsForUserCommand;
import com.fpt.h2s.utilities.SpringBeans;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transaction")
@SuppressWarnings("unused")
public class TransactionController {

    @GetMapping("/user")
    @Operation(summary = "view transactions for user")
    public ApiResponse<ListResult<TransactionResponse>> viewListTransactionOfUser(final ViewTransactionsForUserCommand.Request request) {
        return SpringBeans.getBean(ViewTransactionsForUserCommand.class).execute(request);
    }

    @PostMapping("/business-admin")
    @RequiredRoles({User.Role.BUSINESS_OWNER, User.Role.BUSINESS_ADMIN})
    @Operation(summary = "View transactions for business owner and business admin")
    public static ApiResponse<ListResult<BusinessAdminTransactionResponse>> viewTransactionsForBusinessOwner(@Valid @RequestBody final BusinessAdminSearchTransactionCommand.BusinessOwnerSearchTransactionCommandRequest request) {
        return SpringBeans.getBean(BusinessAdminSearchTransactionCommand.class).execute(request);
    }
}
