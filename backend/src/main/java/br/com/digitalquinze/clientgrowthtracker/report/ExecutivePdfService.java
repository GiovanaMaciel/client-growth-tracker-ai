
package br.com.digitalquinze.clientgrowthtracker.report;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import br.com.digitalquinze.clientgrowthtracker.analytics.DashboardService;
import br.com.digitalquinze.clientgrowthtracker.analytics.dto.DashboardResponse;
import br.com.digitalquinze.clientgrowthtracker.analytics.dto.PeriodSummaryResponse;
import br.com.digitalquinze.clientgrowthtracker.funnel.dto.FunnelResponse;
import br.com.digitalquinze.clientgrowthtracker.funnel.dto.FunnelStageResponse;
import br.com.digitalquinze.clientgrowthtracker.paidtraffic.dto.PaidTrafficMetricResponse;

@Service
public class ExecutivePdfService {

    private static final Locale BRAZIL = Locale.of("pt", "BR");

    private final DashboardService dashboardService;

    public ExecutivePdfService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    public byte[] generate(Long restaurantId, Long periodId) {
        DashboardResponse dashboard =
                dashboardService.getDashboard(restaurantId, periodId);

        try (PDDocument document = new PDDocument()) {

            try (PdfLayout layout = new PdfLayout(document)) {

                PeriodSummaryResponse summary = dashboard.summary();

                String referenceMonth = summary.referenceMonth()
                        .format(DateTimeFormatter.ofPattern("MMMM yyyy", BRAZIL));

                layout.title("RELATORIO EXECUTIVO");

                layout.text(
                        "Restaurante: " + dashboard.restaurant().name()
                );

                layout.text(
                        "Periodo: " + referenceMonth
                );

                layout.text(
                        "Emitido em: " +
                        LocalDate.now(ZoneId.of("America/Sao_Paulo"))
                                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                );

                layout.section("RESUMO FINANCEIRO");

                layout.text(
                        "Faturamento bruto: " +
                        money(summary.totalGrossRevenue())
                );

                layout.text(
                        "Total de pedidos: " + summary.totalOrders()
                );

                layout.text(
                        "Ticket medio consolidado: " +
                        money(summary.consolidatedAverageTicket())
                );

                layout.text(
                        "Liquido recebido: " +
                        money(summary.totalNetRevenue())
                );

                layout.text(
                        "Percentual recebido: " +
                        percent(summary.receivedPercentage())
                );

                layout.text(
                        "Media de pedidos por dia: " +
                        decimal(summary.averageOrdersPerDay())
                );

                layout.text(
                        "Dias de funcionamento: " +
                        summary.openDays()
                );

                layout.section("DESEMPENHO POR CANAL");

                if (summary.channels().isEmpty()) {
                    layout.text("Nenhum canal possui dados neste periodo.");
                } else {
                    summary.channels().forEach(channel -> {
                        try {
                            layout.subsection(channel.channelName());

                            layout.text(
                                    "Faturamento: " +
                                    money(channel.grossRevenue())
                            );

                            layout.text(
                                    "Pedidos: " + channel.orders()
                            );

                            layout.text(
                                    "Ticket medio: " +
                                    money(channel.averageTicket())
                            );

                            layout.text(
                                    "Participacao nas vendas: " +
                                    percent(channel.salesSharePercentage())
                            );

                            layout.text(
                                    "Liquido recebido: " +
                                    money(channel.netRevenue())
                            );

                        } catch (IOException exception) {
                            throw new PdfGenerationException(exception);
                        }
                    });
                }

                layout.section("FUNIS DE CONVERSAO");

                List<FunnelResponse> funnels = dashboard.funnels();

                if (funnels == null || funnels.isEmpty()) {
                    layout.text("Nenhum funil disponivel neste periodo.");
                } else {
                    for (FunnelResponse funnel : funnels) {
                        layout.subsection(funnel.channelName());

                        for (FunnelStageResponse stage : funnel.stages()) {
                            String value =
                                    stage.value() == null
                                            ? "Nao informado"
                                            : stage.value().toString();

                            String conversion =
                                    stage.conversionFromVisits() == null
                                            ? ""
                                            : " | Conversao desde visitas: " +
                                              percent(stage.conversionFromVisits());

                            layout.text(
                                    stage.stageName() +
                                    ": " + value + conversion
                            );
                        }
                    }
                }

                layout.section("TRAFEGO PAGO");

                PaidTrafficMetricResponse traffic = dashboard.paidTraffic();

                if (traffic == null) {
                    layout.text(
                            "Nenhum dado de trafego pago cadastrado."
                    );
                } else {

                    layout.text(
                            "Investimento: " +
                            money(traffic.investment())
                    );

                    layout.text(
                            "Receita atribuida: " +
                            money(traffic.attributedRevenue())
                    );

                    layout.text(
                            "Compras atribuidas: " +
                            traffic.purchases()
                    );

                    layout.text(
                            "ROAS: " + decimal(traffic.roas()) + "x"
                    );

                    layout.text(
                            "CPA: " + money(traffic.cpa())
                    );

                    layout.text(
                            "Frequencia: " +
                            decimal(traffic.frequency())
                    );

                    layout.text(
                            "Custo por visita: " +
                            money(traffic.costPerVisit())
                    );

                    layout.text(
                            "Conversao de visitas em compras: " +
                            percent(traffic.visitToPurchaseConversion())
                    );
                }

                layout.section("OBSERVACOES");

                layout.text(
                        "O valor liquido recebido nao representa " +
                        "necessariamente o lucro do restaurante. " +
                        "Para calcular lucro, tambem devem ser " +
                        "considerados os custos operacionais."
                );

                layout.text(
                        "Relatorio gerado automaticamente pelo " +
                        "Client Growth Tracker - Digital Quinze."
                );
            }

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();

            document.save(output);

            return output.toByteArray();

        } catch (IOException exception) {
            throw new PdfGenerationException(exception);
        }
    }

    private static String money(BigDecimal value) {
        if (value == null) {
            return "Nao informado";
        }

        return java.text.NumberFormat
                .getCurrencyInstance(BRAZIL)
                .format(value);
    }

    private static String decimal(BigDecimal value) {
        if (value == null) {
            return "Nao informado";
        }

        return value
                .setScale(2, RoundingMode.HALF_UP)
                .toPlainString()
                .replace(".", ",");
    }

    private static String percent(BigDecimal value) {
        if (value == null) {
            return "Nao informado";
        }

        return decimal(value) + "%";
    }

    private static class PdfGenerationException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        PdfGenerationException(IOException cause) {
            super("Could not generate the PDF report", cause);
        }
    }

    private static class PdfLayout implements AutoCloseable {

        private static final float MARGIN = 48;
        private static final float BOTTOM_MARGIN = 60;

        private static final Color BRAND_COLOR =
                new Color(15, 118, 110);

        private final PDDocument document;
        private final PDFont regular;
        private final PDFont bold;

        private PDPageContentStream stream;
        private float y;

        PdfLayout(PDDocument document) throws IOException {
            this.document = document;

            this.regular = new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA
            );

            this.bold = new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA_BOLD
            );

            newPage();
        }

        private void newPage() throws IOException {
            if (stream != null) {
                stream.close();
            }

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            stream = new PDPageContentStream(document, page);

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            stream.setNonStrokingColor(BRAND_COLOR);
            stream.addRect(0, pageHeight - 25, pageWidth, 25);
            stream.fill();

            y = pageHeight - 65;
        }

        void title(String value) throws IOException {
            write(value, bold, 19, BRAND_COLOR, 28);
            y -= 10;
        }

        void section(String value) throws IOException {
            y -= 14;
            write(value, bold, 12, BRAND_COLOR, 20);
            y -= 5;
        }

        void subsection(String value) throws IOException {
            y -= 8;
            write(value, bold, 11, Color.DARK_GRAY, 18);
        }

        void text(String value) throws IOException {
            write(value, regular, 10, Color.DARK_GRAY, 15);
        }

        private void write(
                String value,
                PDFont font,
                float fontSize,
                Color color,
                float lineHeight
        ) throws IOException {

            String safeText = sanitize(value);

            float maxWidth =
                    PDRectangle.A4.getWidth() - (MARGIN * 2);

            for (String line : wrap(safeText, font, fontSize, maxWidth)) {

                if (y < BOTTOM_MARGIN + lineHeight) {
                    newPage();
                }

                stream.beginText();
                stream.setNonStrokingColor(color);
                stream.setFont(font, fontSize);
                stream.newLineAtOffset(MARGIN, y);
                stream.showText(line);
                stream.endText();

                y -= lineHeight;
            }
        }

        private String sanitize(String value) {
            if (value == null || value.isBlank()) {
                return "-";
            }

            String normalized = value
                    .replace('\n', ' ')
                    .replace('\r', ' ')
                    .replace('\t', ' ')
                    .replace('—', '-')
                    .replace('–', '-');

            StringBuilder result = new StringBuilder();

            for (int i = 0; i < normalized.length(); i++) {
                String character =
                        String.valueOf(normalized.charAt(i));

                try {
                    regular.getStringWidth(character);
                    result.append(character);

                } catch (IOException | IllegalArgumentException exception) {
                    result.append('?');
                }
            }

            return result.toString();
        }

        private List<String> wrap(
                String text,
                PDFont font,
                float fontSize,
                float maxWidth
        ) throws IOException {

            List<String> lines = new ArrayList<>();

            StringBuilder current = new StringBuilder();

            for (char character : text.toCharArray()) {
                String next = current.toString() + character;

                float width =
                        font.getStringWidth(next) / 1000 * fontSize;

                if (width > maxWidth && !current.isEmpty()) {
                    lines.add(current.toString().trim());
                    current.setLength(0);
                }

                current.append(character);
            }

            if (!current.isEmpty()) {
                lines.add(current.toString().trim());
            }

            return lines;
        }

        @Override
        public void close() throws IOException {
            if (stream != null) {
                stream.close();
                stream = null;
            }
        }
    }
}
