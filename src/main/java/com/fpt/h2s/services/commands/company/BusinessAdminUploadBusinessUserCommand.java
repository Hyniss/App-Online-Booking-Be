package com.fpt.h2s.services.commands.company;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.Company;
import com.fpt.h2s.models.entities.User;
import com.fpt.h2s.models.entities.UserProfile;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.CompanyRepository;
import com.fpt.h2s.repositories.UserProfileRepository;
import com.fpt.h2s.repositories.UserRepository;
import com.fpt.h2s.services.AmazonS3Service;
import com.fpt.h2s.services.commands.BaseCommand;
import com.fpt.h2s.services.commands.company.utils.ExcelUtils;
import com.fpt.h2s.services.commands.responses.BusinessAdminUploadBusinessUserFailedResponse;
import com.fpt.h2s.services.commands.responses.BusinessAdminUploadBusinessUserResponse;
import com.fpt.h2s.services.commands.responses.BusinessAdminUploadBusinessUserSucceedResponse;
import com.fpt.h2s.utilities.FileInfo;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fpt.h2s.services.commands.responses.BusinessAdminUploadBusinessUserFailedResponse.*;
import static com.fpt.h2s.services.commands.responses.BusinessAdminUploadBusinessUserResponse.*;
import static com.fpt.h2s.utilities.FileInfo.Size.MB;

@Log4j2
@Service
@Transactional
@RequiredArgsConstructor
public class BusinessAdminUploadBusinessUserCommand implements BaseCommand<MultipartFile, BusinessAdminUploadBusinessUserSucceedResponse> {

    private final int BATCH_SIZE = 50;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final UserProfileRepository userProfileRepository;
    private final AmazonS3Service amazonS3Service;

    @Override
    public ApiResponse<BusinessAdminUploadBusinessUserSucceedResponse> execute(final MultipartFile file) {
        final Company company = this.companyRepository.findById(getCompanyId()).orElseThrow();
        if(company.getStatus() != Company.Status.ACTIVE) {
            return ApiResponse.badRequest("Công ty của bạn chưa được kích hoạt để thêm thành viên.");
        }

        if (file.isEmpty()) {
            return ApiResponse.badRequest("Xin vui lòng chọn file cần tải lên.");
        }
        if (!FileInfo.Type.EXCEL.isTypeOf(file)) {
            throw ApiException.badRequest("Định dạng tệp phải có đuôi xlsx, xls");
        }

        long sizeInBytes = file.getSize();
        long MAX_SIZE_IN_BYTE = FileInfo.Size.bytesOf(10, MB);
        if (sizeInBytes > MAX_SIZE_IN_BYTE) {
            throw ApiException.badRequest("Dung lượng file không được vượt quá {} MB", MB.ofBytes(MAX_SIZE_IN_BYTE));
        }

        final List<Integer> responses = new ArrayList<>();
        final List<BusinessAdminUploadBusinessUserFailedResponse> failedResponses = this.importExcelFile(file, responses);

        String failedExcelData = null;
        if (!failedResponses.isEmpty()) {
            failedExcelData = amazonS3Service.uploadFile(exportFailedData(failedResponses)).orElseThrow(() -> ApiException.badRequest("File exception."));
        }

        return ApiResponse.success(BusinessAdminUploadBusinessUserSucceedResponse
                .of(failedExcelData, responses));
    }

    private List<BusinessAdminUploadBusinessUserFailedResponse> importExcelFile(final @NonNull MultipartFile file,
                                                                                final List<Integer> responses) {
        AtomicInteger batchCounter = new AtomicInteger(0);

        List<BusinessAdminUploadBusinessUserResponse> batchResponses = new ArrayList<>();
        List<BusinessAdminUploadBusinessUserFailedResponse> failedResponses = new ArrayList<>();

        List<String> listEmails = new ArrayList<>();
        List<String> listPhones = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<String> columnNames = ExcelUtils.getColumnNames(sheet);
            if (columnNames == null || columnNames.size() != 5) {
                throw ApiException.badRequest("Xin vui lòng kiểm tra lại thông tin và sử dụng đúng file mà chúng tôi cung cấp.");
            }

            if (isRowEmpty(sheet.getRow(6))) {
                throw ApiException.badRequest("Xin vui lòng điền toàn bộ thông tin trước khi đưa file lên hệ thống.");
            }

            sheet.forEach(row -> {
                if (row.getRowNum() > 5 && !isRowEmpty(row)) {
                    final BusinessAdminUploadBusinessUserResponse response = createRequestFromRow(row, columnNames, failedResponses, listEmails, listPhones);
                    if (response != null) {
                        batchResponses.add(response);
                        listEmails.add(response.getEmail());
                        listPhones.add(response.getPhone());
                        if (batchCounter.incrementAndGet() == BATCH_SIZE) {
                            responses.addAll(saveBatch(batchResponses).stream().map(User::getId).toList());
                            batchResponses.clear();
                            listEmails.clear();
                            listPhones.clear();
                            batchCounter.set(0);
                        }
                    }
                }
            });
            if (!batchResponses.isEmpty()) {
                responses.addAll(saveBatch(batchResponses).stream().map(User::getId).toList());
                listEmails.clear();
                listPhones.clear();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return failedResponses;
    }

    private List<User> saveBatch(final List<BusinessAdminUploadBusinessUserResponse> batchResponses) {
        List<User> users = this.userRepository.saveAll(batchResponses.stream()
                .map(response -> response.toUser(getCompanyId()))
                .collect(Collectors.toList()));

        if(!users.isEmpty()) {
            this.userProfileRepository.saveAll(users.stream()
                    .flatMap(user -> Stream.of(UserProfile.builder().userId(user.getId()).build())).collect(Collectors.toList()));
        }
        return users;
    }

    private BusinessAdminUploadBusinessUserResponse createRequestFromRow(final Row row,
                                                                         final @NotNull List<String> columnNames,
                                                                         final List<BusinessAdminUploadBusinessUserFailedResponse> failedResponses,
                                                                         final List<String> listEmails,
                                                                         final List<String> listPhones) {
        final Map<String, String> cellValues = new HashMap<>();
        row.forEach(cell -> {
            int cellIndex = cell.getColumnIndex();
            String columnName = columnNames.get(cellIndex);
            String cellValue = validateAndExtractValue(cell, columnName);
            cellValues.put(columnName, cellValue);
        });
        return extracted(failedResponses, cellValues, listEmails, listPhones);
    }

    private BusinessAdminUploadBusinessUserResponse extracted(final List<BusinessAdminUploadBusinessUserFailedResponse> failedResponses,
                                                              final Map<String, String> cellValues,
                                                              final List<String> listEmails,
                                                              final List<String> listPhones) {
        String region = cellValues.get("Region") == null ? "Lỗi: Xin vui lòng không để trống" : cellValues.get("Region");
        String phone = cellValues.get("Phone") == null  ? "Lỗi: Xin vui lòng không để trống" : cellValues.get("Phone");
        String email = cellValues.get("Email") == null  ? "Lỗi: Xin vui lòng không để trống" : cellValues.get("Email");
        String username = cellValues.get("Username") == null  ? "Lỗi: Xin vui lòng không để trống" : cellValues.get("Username");
        String role = cellValues.get("System role") == null  ? "Lỗi: Xin vui lòng không để trống" : cellValues.get("System role");
        if(isPrototypeData(email, phone, username, role)) {
            return null;
        }
        if (this.userRepository.existsByEmail(email) || listEmails.contains(email)) {
            email = "Lỗi: Email đã tồn tại: " + email;
        }
        if (this.userRepository.existsByPhone(toPhone(region, phone)) || listPhones.contains(phone)) {
            phone = "Lỗi: Số điện thoại đã tồn tại: " + phone;
        }
        if (!isValidData(email, region, phone, username, role)) {
            failedResponses.add(of(email, region, phone, username, role));
            return null;
        }
        return BusinessAdminUploadBusinessUserResponse.builder()
                .email(email)
                .phone(toPhone(region, phone))
                .username(username)
                .role(User.Role.valueOf(role))
                .status(User.Status.PENDING)
                .build();
    }

    private Integer getCompanyId() {
        final Integer businessAdminId = User.currentUserId()
                .orElseThrow(() -> ApiException.badRequest("Phiên đăng nhập hết hạn. Xin vui lòng đăng nhập."));
        final User businessAdmin = this.userRepository.findById(businessAdminId)
                .orElseThrow(() -> ApiException.badRequest("Không thể tìm thấy người dùng với id = {}.", businessAdminId));
        return businessAdmin.getCompanyId();
    }
}
