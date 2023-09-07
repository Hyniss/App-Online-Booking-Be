package com.fpt.h2s.controllers;

import ananta.utility.StringEx;
import com.fasterxml.jackson.databind.JsonNode;
import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.models.entities.Category;
import com.fpt.h2s.models.entities.DayType;
import com.fpt.h2s.models.exceptions.ApiException;
import com.fpt.h2s.repositories.CategoryRepository;
import com.fpt.h2s.repositories.DayTypeRepository;
import com.fpt.h2s.services.AmazonS3Service;
import com.fpt.h2s.utilities.FileInfo;
import com.fpt.h2s.utilities.FileInfo.Size;
import com.fpt.h2s.utilities.SpringBeans;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.fpt.h2s.utilities.FileInfo.Size.MB;

@RestController
@RequiredArgsConstructor
@RequestMapping("/utility")
public class HelperController {

    @PutMapping("/import-category")
    @Operation(summary = "Import category to database")
    public ApiResponse<Void> changeStatusAccommodation(final MultipartFile file, Category.Type type) {
        String url = SpringBeans.getBean(AmazonS3Service.class).uploadFile(file).orElseThrow();
        String name = StringEx.beforeLastOf(".", file.getOriginalFilename());
        Category category = Category.builder().image(url).name(name).type(type).build();
        SpringBeans.getBean(CategoryRepository.class).save(category);
        return ApiResponse.success(url);
    }

    @PostMapping("/upload/image")
    @Operation(summary = "Upload image")
    public ApiResponse<String> uploadFile(final MultipartFile file) {
        if (!FileInfo.Type.IMAGE.isTypeOf(file)) {
            throw ApiException.badRequest("Định dạng tệp phải là ảnh");
        }

        long sizeInBytes = file.getSize();
        long MAX_SIZE_IN_BYTE = Size.bytesOf(5, MB);

        if (sizeInBytes > MAX_SIZE_IN_BYTE) {
            throw ApiException.badRequest("Dung lượng ảnh không được vượt quá {} MB", MB.ofBytes(MAX_SIZE_IN_BYTE));
        }
        String fileName = SpringBeans.getBean(AmazonS3Service.class).uploadFile(file).orElseThrow(() -> ApiException.failed("Lưu ảnh không thành công"));
        return ApiResponse.success("success", fileName);
    }

    @PostMapping("/add/day-type")
    @Operation(summary = "Add day type to database")
    public ApiResponse<Void> addDayType(@RequestBody final JsonNode requestBody) {
        int year = requestBody.get("year").asInt();
        LocalDate date = LocalDate.of(year, 1, 1);
        LocalDate endDate = date.plusYears(5).with(TemporalAdjusters.lastDayOfYear());
        List<DayType> calendarEntries = new ArrayList<>();
        while (!date.isAfter(endDate)) {
            DayType.Type dayType = DayType.Type.WEEKDAY;
            if (isSpecialDay(date)) {
                dayType = DayType.Type.SPECIAL_DAY;
            } else if (date.getDayOfWeek().getValue() >= DayOfWeek.FRIDAY.getValue() &&
                date.getDayOfWeek().getValue() <= DayOfWeek.SUNDAY.getValue()) {
                dayType = DayType.Type.WEEKEND;
            }
            calendarEntries.add(DayType.builder().date(date).type(dayType).build());
            date = date.plusDays(1);
        }
        SpringBeans.getBean(DayTypeRepository.class).saveAll(calendarEntries);
        return ApiResponse.success();
    }

    private boolean isSpecialDay(LocalDate date) {
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        return (month == 1 && day == 1)
            || (month == 9 && day == 2)
            || (month == 4 && day == 30)
            || (month == 5 && day == 1);
    }

    @PutMapping("/update/day-type")
    @Operation(summary = "Update special day in day type")
    public ApiResponse<Void> updateDayType(@RequestBody final List<Map<String, Object>> dayTypesList) {
        dayTypesList.forEach(dayTypeMap -> {
            LocalDate date = LocalDate.parse((String) dayTypeMap.get("date"));
            DayType.Type type = DayType.Type.valueOf((String) dayTypeMap.get("type"));
            SpringBeans.getBean(DayTypeRepository.class).findById(date)
                .ifPresent(dayType -> {
                    dayType.setType(type);
                    SpringBeans.getBean(DayTypeRepository.class).save(dayType);
                });
        });
        return ApiResponse.success();
    }

    @PutMapping("/update/day-type/tet")
    @Operation(summary = "Update special day in day type")
    public ApiResponse<Void> updateDayType(@RequestBody Map<String, Object> requestBody) {
        String fromDateStr = (String) requestBody.get("fromDate");
        String toDateStr = (String) requestBody.get("toDate");
        DayType.Type type = DayType.Type.valueOf((String) requestBody.get("type"));

        LocalDate fromDate = LocalDate.parse(fromDateStr);
        LocalDate toDate = LocalDate.parse(toDateStr);

        List<DayType> dayTypesToUpdate = SpringBeans.getBean(DayTypeRepository.class).findAll()
            .stream()
            .filter(dayType -> {
                LocalDate date = dayType.getDate();
                return (date.isEqual(fromDate) || date.isEqual(toDate) || (date.isAfter(fromDate) && date.isBefore(toDate)));
            })
            .collect(Collectors.toList());

        dayTypesToUpdate.forEach(dayType -> {
            dayType.setType(type);
            SpringBeans.getBean(DayTypeRepository.class).save(dayType);
        });

        return ApiResponse.success();
    }

}
