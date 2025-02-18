package com.qrmenu.dto.menu;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Category creation/update request")
public class CategoryRequest {
    @Schema(description = "Category name", example = "Main Course")
    @NotBlank(message = "Category name is required")
    private String name;

    @Schema(description = "Category description", example = "Our signature main dishes")
    private String description;

    @Schema(description = "Display order for sorting", example = "1")
    @NotNull(message = "Display order is required")
    private Integer displayOrder;

    @Schema(description = "Category visibility status", example = "true")
    private boolean active = true;
} 