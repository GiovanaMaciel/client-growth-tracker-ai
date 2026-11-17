
package br.com.digitalquinze.clientgrowthtracker.report;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(
    "/api/restaurants/{restaurantId}/periods/{periodId}/dashboard"
)
public class ExecutivePdfController {

    private final ExecutivePdfService pdfService;

    public ExecutivePdfController(ExecutivePdfService pdfService) {
        this.pdfService = pdfService;
    }

    @GetMapping(value = "/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> download(
            @PathVariable Long restaurantId,
            @PathVariable Long periodId) {

        byte[] pdf = pdfService.generate(restaurantId, periodId);

        String filename =
                "relatorio-restaurante-" +
                restaurantId +
                "-periodo-" +
                periodId +
                ".pdf";

        ContentDisposition disposition =
                ContentDisposition.attachment()
                        .filename(filename)
                        .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        disposition.toString()
                )
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .contentLength(pdf.length)
                .body(pdf);
    }
}
