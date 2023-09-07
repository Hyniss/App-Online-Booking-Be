package com.fpt.h2s.services.commands.user;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.requests.ChangeUserStatusRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChangeUserStatusCommand implements BaseCommand<ChangeUserStatusRequest, Void> {
    private final UserRepository userRepository;
    
    @Override
    public ApiResponse<Void> execute(final ChangeUserStatusRequest request) {
        final User user = this.userRepository
            .findById(request.getId())
            .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy người dùng với id = {}", request.getId()));
        final User userToDisable = user.toBuilder().status(request.getStatus()).build();
        this.userRepository.save(userToDisable);
        return ApiResponse.success("Thay đổi trạng thái người dùng thành công.");
    }
}
