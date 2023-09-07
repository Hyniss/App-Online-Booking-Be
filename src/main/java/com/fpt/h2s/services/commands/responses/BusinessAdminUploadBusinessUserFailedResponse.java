package com.fpt.h2s.services.commands.responses;

import com.fpt.h2s.services.commands.company.utils.ExcelUtils;
import com.fpt.h2s.services.commands.company.utils.MultipartFileUtils;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Builder
public class BusinessAdminUploadBusinessUserFailedResponse {
    private String email;

    private String region;

    private String phone;

    private String username;

    private String role;

    public static BusinessAdminUploadBusinessUserFailedResponse of(String email,
                                                                   String region,
                                                                   String phone,
                                                                   String username,
                                                                   String role) {
        return BusinessAdminUploadBusinessUserFailedResponse.builder()
                .email(email)
                .region(region)
                .phone(phone)
                .username(username)
                .role(role)
                .build();
    }

    public static boolean isValidData(String email, String region, String phone,  String name, String role) {
        return !email.contains("Lỗi:")
                && !region.contains("Lỗi:")
                && !phone.contains("Lỗi:")
                && !name.contains("Lỗi:")
                && !role.contains("Lỗi:");
    }

    public static boolean isPrototypeData(String email, String phone,  String name, String role) {
        return email.equals("home2stay@gmail.com")
                && phone.equals("0912372062")
                && name.equals("Nguyễn Văn A")
                && role.equals("BUSINESS_MEMBER");
    }

    public static MultipartFile exportFailedData(final List<BusinessAdminUploadBusinessUserFailedResponse> failedResponses) {
            String filename = "error.xlsx";
            String contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        return MultipartFileUtils.convertToMultipartFile(ExcelUtils.exportToExcel(failedResponses), filename, contentType);
    }
}
