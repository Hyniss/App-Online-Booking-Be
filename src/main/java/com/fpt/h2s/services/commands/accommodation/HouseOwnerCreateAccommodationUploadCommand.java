package com.fpt.h2s.services.commands.accommodation;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.services.AmazonS3Service;
import com.fpt.h2s.services.commands.BaseCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HouseOwnerCreateAccommodationUploadCommand
        implements BaseCommand<List<MultipartFile>, List<String>> {

    private final AmazonS3Service amazonS3Service;
    @Override
    public ApiResponse<List<String>> execute(final List<MultipartFile> requests) {

        List<String> fileNames = requests.stream()
                .map(file -> this.amazonS3Service.uploadFile(file).orElseThrow())
                .collect(Collectors.toList());
        return ApiResponse.success(fileNames);
    }
}
