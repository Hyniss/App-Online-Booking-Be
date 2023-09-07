package com.fpt.h2s.services.commands.transactions;

import ananta.utility.ListEx;
import ananta.utility.MapEx;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.ListResult;
import com.fpt.h2s.models.domains.SearchRequest;
import com.fpt.h2s.models.entities.Transaction;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.repositories.TransactionRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.responses.TransactionResponse;
import com.fpt.h2s.services.commands.responses.UserResponse;
import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import lombok.experimental.FieldNameConstants;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Transactional
@RequiredArgsConstructor
@Service
public class ViewTransactionsForUserCommand implements BaseCommand<ViewTransactionsForUserCommand.Request, ListResult<TransactionResponse>> {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public ApiResponse<ListResult<TransactionResponse>> execute(final Request request) {
        final Integer userId = User.currentUserId().orElseThrow();

        final Page<Transaction> page = this.transactionRepository.findAllOfUser(userId, request.toPageRequest());
        final List<TransactionResponse> responses = this.getTransactionResponses(page.getContent());

        return ApiResponse.success(ListResult.of(page).withContent(responses));
    }
    @NotNull
    private List<TransactionResponse> getTransactionResponses(final List<Transaction> transactions) {
        final List<Integer> creatorIds = ListEx.listOf(transactions, Transaction::getCreatorId);
        final List<Integer> receiverIds = ListEx.listOf(transactions, Transaction::getReceiverId);

        final Set<Integer> userIds = Stream
            .concat(creatorIds.stream(), receiverIds.stream())
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableSet());

        final Map<Integer, UserResponse> userResponseMapToId = MapEx.mapOf(this.userRepository.findAllById(userIds), User::getId, UserResponse::of);

        return transactions.stream()
            .map(transaction -> TransactionResponse
                .of(transaction)
                .withCreator(userResponseMapToId.get(transaction.getCreatorId()))
                .withReceiver(userResponseMapToId.get(transaction.getReceiverId()))
            )
            .toList();
    }

    @Builder
    @Getter
    @FieldNameConstants
    @Jacksonized
    @With
    public static class Request extends SearchRequest {
        private String orderBy;
        private Boolean isDescending;
        private Integer size;
        private Integer page;
    }
}
