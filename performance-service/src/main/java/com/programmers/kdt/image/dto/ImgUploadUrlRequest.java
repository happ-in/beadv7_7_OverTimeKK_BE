package com.programmers.kdt.image.dto;

import jakarta.validation.constraints.NotNull;

public record ImgUploadUrlRequest(
        @NotNull String fileName,
        @NotNull String contentType
) {
}
