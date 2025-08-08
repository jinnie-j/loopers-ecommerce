package com.loopers.interfaces.api.brand;

import com.loopers.domain.brand.BrandCommand;
import jakarta.validation.constraints.NotBlank;

public class BrandRequest {
    public record Create(
        @NotBlank String name,
        String description
        ){
        public BrandCommand.Create toCommand(){
            return new BrandCommand.Create(name, description);
        }
    }
}
