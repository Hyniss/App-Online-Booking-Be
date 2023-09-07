package com.fpt.h2s.services.commands.responses;

import ananta.utility.StringEx;
import com.fpt.h2s.models.entities.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static com.fpt.h2s.models.entities.User.Role.BUSINESS_ADMIN;
import static com.fpt.h2s.models.entities.User.Role.BUSINESS_MEMBER;

@Getter
@Setter
@Builder
public class BusinessAdminUploadBusinessUserResponse {
    private Integer userId;

    private String email;

    private String region;

    private String phone;

    private String username;

    private User.Role role;

    private User.Status status;

    private String password;

    private Integer companyId;


    public static String validateAndExtractValue(final Cell cell, final String columnName) {
        if (cell != null) {
            DataFormatter dataFormatter = new DataFormatter();
            String cellValue = dataFormatter.formatCellValue(cell).trim();
            switch (columnName) {
                case "Email" -> {
                    return getEmailValue(cellValue);
                }
                case "Username" -> {
                    return getUsername(cellValue);
                }
                case "System role" -> {
                    return getSystemRole(cellValue);
                }
                case "Phone" -> {
                    return getPhoneValue(cellValue);
                }
                case "Region" -> {
                    return getRegionValue(cellValue);
                }
            }
        }
        return "";
    }

    private static String getUsername(final String value) {
        if(value.isEmpty()) {
            return "Lỗi: Xin vui lòng không để trống";
        }
        if (value.length() <= 32 && value.length() >= 4 && value.matches("^[\\p{L}\\s'-]+$")) {
            return value;
        }
        if(value.contains("Lỗi:")) {
            return value;
        }
        return "Lỗi: Xin hãy nhập tên người dùng trong khoảng 4 đến 32 ký tự, chỉ chứa a-z, A-Z và khoảng trắng " + value;
    }

    private static String getSystemRole(final String value) {
        if(value.isEmpty()) {
            return "Lỗi: Xin vui lòng không để trống";
        }
        final Set<User.Role> allowedRoles = Set.of(BUSINESS_ADMIN, BUSINESS_MEMBER);
        if (allowedRoles.contains(User.Role.valueOf(value))) {
            return value;
        }
        if(value.contains("Lỗi:")) {
            return value;
        }
        return "Lỗi: Hệ thống của chúng tôi hiện tại chỉ được chọn một trong hai quyền sau đây là BUSINESS_ADMIN hoặc BUSINESS_MEMBER";
    }

    private static String getPhoneValue(final String value) {
        String regex = "^(0?)(3[2-9]|5[6|8|9]|7[0|6-9]|8[0-6|8|9]|9[0-4|6-9])[0-9]{7}$";
        Matcher matcher = Pattern.compile(regex).matcher(value);
        if(value.isEmpty()) {
            return "Lỗi: Xin vui lòng không để trống";
        }
        if (matcher.matches() || value.contains("Lỗi:")) {
            return value;
        }
        return "Lỗi: Số điện thoại không đúng định dạng: " + value;
    }

    private static String getRegionValue(final String value) {
        if(value.isEmpty()) {
            return "Lỗi: Xin vui lòng không để trống";
        }
        String regex = "^(\\+?\\d{1,3}|\\d{1,4})$";
        Matcher matcher = Pattern.compile(regex).matcher(value);
        if (matcher.matches() || value.contains("Lỗi:")) {
            return value;
        }
        return "Lỗi: Mã vùng điện thoại không đúng định dạng: " + value;
    }

    private static String getEmailValue(final String value) {
        if(value.isEmpty()) {
            return "Lỗi: Xin vui lòng không để trống";
        }
        String regex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        Matcher matcher = Pattern.compile(regex).matcher(value);
        if (matcher.matches() || value.contains("Lỗi:")) {
            return value;
        }
        return "Lỗi: Email không đúng định dạng: " + value;
    }

    public static boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }

        return IntStream.rangeClosed(1, 6)
                .mapToObj(row::getCell)
                .allMatch(BusinessAdminUploadBusinessUserResponse::isCellEmpty);
    }

    private static boolean isCellEmpty(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return true;
        }
        return cell.getCellType() == CellType.STRING && cell.getStringCellValue().trim().isEmpty();
    }

    public static String toPhone(final String region, final String phone) {
        return StringEx.format("+{} {}", region, phone.length() == 10 ? phone.substring(1) : phone);
    }

    public User toUser(final Integer companyId) {
        return User.builder()
                .email(this.email)
                .phone(this.phone)
                .username(this.username)
                .roles(role.name())
                .status(User.Status.PENDING)
                .password(this.password)
                .companyId(companyId)
                .build();
    }
}
