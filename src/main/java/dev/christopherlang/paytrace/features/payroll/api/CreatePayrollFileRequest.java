package dev.christopherlang.paytrace.features.payroll.api;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePayrollFileRequest {

    @NotNull
    private MultipartFile file;

}
