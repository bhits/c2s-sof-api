package gov.samhsa.c2s.c2ssofapi.service.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder
public class PdfDto {
    private byte[] pdfBytes;

    public PdfDto(byte[] pdfBytes) {
        this.pdfBytes = pdfBytes;
    }
}
