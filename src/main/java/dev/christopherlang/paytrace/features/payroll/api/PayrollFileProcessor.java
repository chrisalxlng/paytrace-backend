package dev.christopherlang.paytrace.features.payroll.api;

import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import dev.christopherlang.paytrace.common.BusinessException;
import dev.christopherlang.paytrace.common.ErrorCode;
import dev.christopherlang.paytrace.features.payroll.domain.Payroll;

import org.apache.pdfbox.Loader;

@Component
public class PayrollFileProcessor {

    private final List<PayrollParser> parsers;

    public PayrollFileProcessor(List<PayrollParser> parsers) {
        this.parsers = parsers;
    }

    String extractText(MultipartFile file) {
        try (PDDocument document = Loader.loadPDF(file.getInputStream().readAllBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PAYROLL_PARSING_FAILED);
        }
    }

    public Payroll processFile(MultipartFile file) {
        String pdfText = extractText(file);

        return parsers.stream()
                .filter(parser -> parser.supports(pdfText))
                .findFirst()
                .orElseThrow(() ->
                        new BusinessException(ErrorCode.UNSUPPORTED_PROVIDER)
                )
                .parse(pdfText);
    }

}
