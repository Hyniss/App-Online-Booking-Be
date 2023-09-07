package com.fpt.h2s.services.commands.user;

import ananta.utility.StringEx;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fpt.h2s.configurations.ConsulConfiguration;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.domains.BaseRequest;
import com.fpt.h2s.models.domains.BaseValidator;
import com.fpt.h2s.models.domains.TokenUser;
import com.fpt.h2s.models.entities.Contract;
import com.fpt.h2s.models.entities.EmailTemplate;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.entities.UserProfile;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.ContractRepository;
import com.fpt.h2s.repositories.RedisRepository;
import com.fpt.h2s.repositories.UserProfileRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.AmazonS3Service;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.user.utils.PdfUtils;
import com.fpt.h2s.workers.MailWorker;
import com.fpt.h2s.utilities.Mappers;
import com.fpt.h2s.utilities.MoreRequests;
import com.fpt.h2s.utilities.Tokens;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.extern.log4j.Log4j2;
import org.hibernate.validator.constraints.Length;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class RegisterToHouseOwnerCommand implements BaseCommand<RegisterToHouseOwnerCommand.Request, Void> {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ContractRepository contractRepository;
    private final ConsulConfiguration consulConfiguration;
    private final MailWorker mailWorker;
    private final AmazonS3Service amazonS3Service;

    @Override
    public ApiResponse<Void> execute(final Request request) {

        final User user = this.userRepository.findById(request.getUserId())
                .orElseThrow(() -> ApiException.badRequest("Không thể người dùng với id = {}.", request.getUserId()));
        final User userToHouseOwner = request.toUser(user);
        User houseOwner = this.userRepository.save(userToHouseOwner);

        final UserProfile userProfile = user.getUserProfile() == null ? request.toUserProfile(user.getId()) : request.toUserProfile(user);
        this.userProfileRepository.save(userProfile);

        updateToken(houseOwner);
        final Contract contractToSave = request.toContract();
        this.contractRepository.save(contractToSave);

        this.sendMail(contractToSave.getContent(), user.getEmail());
        return ApiResponse.success("Đăng kí làm người cho thuê nhà thành công.");
    }

    private void updateToken(User houseOwner) {
        final String newToken = Tokens.generateToken(Mappers.mapOf(TokenUser.of(houseOwner)), this.consulConfiguration.get("secret-key.AUTH_TOKEN"));
        RedisRepository.set(newToken, houseOwner);

        String currentToken = Tokens.getTokenFrom(MoreRequests.getCurrentHttpRequest());
        String tokenKey = StringEx.format("{}-alt-token", currentToken);

        RedisRepository.set(tokenKey, newToken);
        RedisRepository.set(newToken, houseOwner);
    }

    public void sendMail(final String content, final String email) {
        String termAndCondition = PdfUtils.ConvertFileContentToString(amazonS3Service);
        byte[] pdfBytes = PdfUtils.generatePdfFromHtml(termAndCondition, content);
        if (pdfBytes.length == 0)  {
            throw ApiException.badRequest("Có gì đó không ổn. Vui lòng tải lại trang.");
        }
         this.mailWorker.sendMail(m -> m
                .sendTo(email)
                .withTemplate(EmailTemplate.Key.REGISTER_TO_HOUSE_OWNER)
                .withAttachment(pdfBytes)
                .withSuccessMessage("Send mail successfully")
        );
    }

    @Getter
    @Builder
    @FieldNameConstants
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, creatorVisibility = JsonAutoDetect.Visibility.ANY)
    public static class Request extends BaseRequest {

        @NonNull
        private Integer userId;

        @NotBlank(message = "Xin vui lòng không để trống tên hợp đồng.")
        @Length(max = 255, message = "Xin vui lòng để tên hợp đồng trong khoảng 512 ký tự")
        private String contractName;

        @NotBlank(message = "Xin vui lòng không để trống nội dung hợp đồng.")
        @Length(max = 65535, message = "Xin vui lòng để nội dung hợp đồng trong khoảng 65535 ký tự.")
        private String content;

        @NotNull(message = "Xin vui lòng không để trống mục phần trăm lợi nhuận.")
        @Min(value = 15, message = "Phần trăm chiết khấu mặc định cho H2S là 15%.")
        @Max(value = 15, message = "Phần trăm chiết khấu mặc định cho H2S là 15%.")
        private Integer profit;

        @NotNull(message = "Xin vui lòng nhập mã ngân hàng.")
        private UserProfile.BankCode bankCode;

        @NotBlank(message = "Xin vui lòng nhập số tài khoản/số thẻ.")
        private String accountNo;

        @NotBlank(message = "Xin vui lòng nhập tên tài khoản/tên chủ thẻ.")
        @Length(min = 4, max = 60, message = "Vui lòng nhập tên chủ thẻ trong khoảng 4 đến 60 kí tự.")
        @Pattern(regexp="^[a-zA-Z\\s]*$", message = "Tên tài khoản/chủ thẻ chỉ có tiếng việt không dấu, không có số hoặc kí tự đặc biệt.")
        private String accountName;

        public Contract toContract() {
            return Contract.builder()
                    .name(this.contractName)
                    .content(this.content)
                    .profit(this.profit)
                    .status(Contract.Status.APPROVED)
                    .expiredAt(null)
                    .build();
        }

        public User toUser(final User user) {
            return user.toBuilder()
                    .roles(String.join(", ", user.getRoles(), User.Role.HOUSE_OWNER.toString()))
                    .build();
        }

        private UserProfile toUserProfile(final User user) {
            return user.getUserProfile().toBuilder()
                    .bankCode(this.bankCode)
                    .accountNo(this.accountNo)
                    .accountType(UserProfile.AccountType.Account)
                    .accountName(accountName.toUpperCase())
                    .build();
        }

        public UserProfile toUserProfile(final Integer userId) {
            return UserProfile.builder()
                    .userId(userId)
                    .bankCode(this.bankCode)
                    .accountNo(accountNo)
                    .accountType(UserProfile.AccountType.Account)
                    .build();
        }

        @Component
        @RequiredArgsConstructor
        public static class Validator extends BaseValidator<RegisterToHouseOwnerCommand.Request> {

            private final UserRepository userRepository;

            @Override
            protected void validate() {
                this.rejectIfEmpty(Fields.userId, this::validateUserId);
                this.rejectIfEmpty(Fields.accountNo, this::validateAccountNo);
            }

            private String validateAccountNo() {
                if(this.request.accountNo.length() < 4 || this.request.accountNo.length() > 22) {
                    return "Xin vui lòng nhập số tài khoản từ 4 đến 22 ký tự.";
                }
                if(!this.request.accountNo.matches("^[0-9]+$")){
                    return "Xin vui lòng chỉ nhập số nguyên! Không nhập chữ cái hay bất kì ký tự đặc biệt nào.";
                }
                return null;
            }

            private String validateUserId() {
                Integer currentUserId = User.currentUserId()
                        .orElseThrow(() -> ApiException.badRequest("Phiên đăng nhập hết hạn. Xin vui lòng đăng nhập."));

                if (!Objects.equals(currentUserId, this.request.userId)) {
                    return "Có lỗi xảy ra. Vui lòng tải lại trang.";
                }

                final User user = this.userRepository.findById(this.request.userId)
                        .orElseThrow(() -> ApiException.badRequest("Không thể người dùng với id = {}.", this.request.userId));

                if(user.is(User.Role.HOUSE_OWNER)) {
                    return "Người dùng đã đăng kí làm người cho thuê nhà.";
                }

                if (user.is(User.Role.BUSINESS_OWNER)
                        || user.is(User.Role.BUSINESS_ADMIN)
                        || user.is(User.Role.BUSINESS_MEMBER)) {
                    return "Người dùng thuộc tài khoản công ty không được phép đăng ký cho thuê nhà.";
                }

                if (user.is(User.Status.BANNED)
                        || user.is(User.Status.PENDING)) {
                    return "Xin vui lòng kiểm tra lại trạng thái tài khoản.";
                }

                return null;
            }
        }
    }
}
