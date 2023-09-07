package com.fpt.h2s.services.commands.room;

import com.fpt.h2s.models.domains.ApiResponse;
import com.fpt.h2s.services.AmazonS3Service;
import com.fpt.h2s.services.commands.BaseCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
@RequiredArgsConstructor
public class HouseOwnerCreateRoomUploadImageCommand implements BaseCommand<MultipartFile, String> {

    private final AmazonS3Service amazonS3Service;
    @Override
    public ApiResponse<String> execute(final MultipartFile request) {
        String fileName = this.amazonS3Service.uploadFile(request).orElseThrow();
        return ApiResponse.success("success",fileName);
    }
}
