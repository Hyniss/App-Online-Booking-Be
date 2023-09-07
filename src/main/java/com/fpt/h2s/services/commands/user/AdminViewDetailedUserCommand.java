package com.fpt.h2s.services.commands.user;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.TransactionRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.requests.AdminViewDetailedUserRequest;
import com.fpt.h2s.services.commands.responses.SpendingResponse;
import com.fpt.h2s.services.commands.responses.UserDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminViewDetailedUserCommand implements BaseCommand<AdminViewDetailedUserRequest, UserDetailResponse> {
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    
    @Override
    public ApiResponse<UserDetailResponse> execute(final AdminViewDetailedUserRequest request) {
        final int id = request.getId();
        final User user = this.userRepository
            .findById(id)
            .orElseThrow(() -> ApiException.badRequest("User not found."));
        
        final List<SpendingResponse> spendingResponses = this.transactionRepository.sumAmountByMonth(LocalDate.now().getYear(), id);
        
        final UserDetailResponse response = UserDetailResponse.of(user, spendingResponses);
        return ApiResponse.success(response);
    }
}
