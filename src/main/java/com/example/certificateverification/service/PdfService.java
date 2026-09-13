package com.example.certificateverification.service;

import com.example.certificateverification.entity.Certificate;
import com.example.certificateverification.entity.CertificateTemplate;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class PdfService {

    private final QrCodeService qrCodeService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy");

    public PdfService(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    public byte[] generateCertificatePdf(Certificate certificate, String verificationUrl) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            // A4 Landscape Document: 841.89 x 595.28 pt with balanced margins
            Document document = new Document(PageSize.A4.rotate(), 44, 44, 34, 30);
            PdfWriter writer = PdfWriter.getInstance(document, out);

            document.open();

            float pageWidth = PageSize.A4.getHeight();  // 841.89 (rotated)
            float pageHeight = PageSize.A4.getWidth();  // 595.28 (rotated)

            // Draw modern geometric accents and border on background
            drawCertificateBackground(writer.getDirectContentUnder(), pageWidth, pageHeight);

            CertificateTemplate template = certificate.getTemplate();

            // 1. TOP HEADER TABLE: [LARGE LOGO (Left)] | [CERT NUMBER & ISSUE DATE (Right)]
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{70, 30});
            headerTable.setSpacingAfter(12f);

            // Top-left Logo (starts flush at top-left, top-aligned with Certificate Number)
            PdfPCell logoCell = new PdfPCell();
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setVerticalAlignment(Element.ALIGN_TOP);
            logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            logoCell.setPadding(0f);

            Image logoImage = resolveImage(template.getLogoPath());
            if (logoImage == null) {
                logoImage = resolveImage("/images/default-logo.png");
            }

            if (logoImage != null) {
                // Significantly enlarged logo size, top-left aligned
                logoImage.setAlignment(Image.ALIGN_LEFT);
                logoImage.scaleToFit(170, 110);
                logoCell.addElement(logoImage);
            } else {
                Paragraph emptyPara = new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 10));
                logoCell.addElement(emptyPara);
            }
            headerTable.addCell(logoCell);

            // Top-right Certificate Metadata (Top-aligned with Logo)
            PdfPCell metaCell = new PdfPCell();
            metaCell.setBorder(Rectangle.NO_BORDER);
            metaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            metaCell.setVerticalAlignment(Element.ALIGN_TOP);
            metaCell.setPadding(0f);
            metaCell.setPaddingRight(2f);

            Font certNumFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(30, 41, 59));
            Font issueDateFont = FontFactory.getFont(FontFactory.HELVETICA, 9.5f, new Color(100, 116, 139));

            Paragraph certNumPara = new Paragraph(certificate.getCertificateNumber(), certNumFont);
            certNumPara.setAlignment(Element.ALIGN_RIGHT);
            certNumPara.setSpacingAfter(3f);
            metaCell.addElement(certNumPara);

            String formattedDate = certificate.getIssueDate() != null
                    ? certificate.getIssueDate().format(DATE_FORMATTER)
                    : LocalDate.now().format(DATE_FORMATTER);
            Paragraph datePara = new Paragraph(formattedDate, issueDateFont);
            datePara.setAlignment(Element.ALIGN_RIGHT);
            metaCell.addElement(datePara);

            headerTable.addCell(metaCell);
            document.add(headerTable);

            // 2. DYNAMIC CERTIFICATE PRINTED TITLE
            // Never hard-coded. Strictly loaded from Certificate Printed Title *
            String printedTitle = (certificate.getCertificateTitle() != null && !certificate.getCertificateTitle().isBlank())
                    ? certificate.getCertificateTitle()
                    : template.getCertificateTitle();
            if (printedTitle == null || printedTitle.isBlank()) {
                printedTitle = "CERTIFICATE OF ACHIEVEMENT";
            }

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 27, new Color(15, 23, 42));
            Paragraph titlePara = new Paragraph(printedTitle.toUpperCase(), titleFont);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            titlePara.setSpacingAfter(14f); // Certificate Title -> Intro text: 14px-18px
            document.add(titlePara);

            // 3. SUBTITLE: "This is to certify that"
            Font introFont = FontFactory.getFont(FontFactory.HELVETICA, 12, new Color(71, 85, 105));
            Paragraph introPara = new Paragraph("This is to certify that", introFont);
            introPara.setAlignment(Element.ALIGN_CENTER);
            introPara.setSpacingAfter(10f); // Intro text -> Student name: 10px-14px
            document.add(introPara);

            // 4. DYNAMIC RECIPIENT NAME
            String studentName = certificate.getStudent() != null ? certificate.getStudent().getName() : "";
            Font nameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 25, new Color(30, 58, 138));
            Paragraph namePara = new Paragraph(studentName, nameFont);
            namePara.setAlignment(Element.ALIGN_CENTER);
            namePara.setSpacingAfter(14f); // Student name -> Description: 14px-18px
            document.add(namePara);

            // 5. CERTIFICATE DESCRIPTION / BODY TEXT
            // Uses Default Template Description / Text as source
            String descriptionText = (template.getDescription() != null && !template.getDescription().isBlank())
                    ? template.getDescription()
                    : (certificate.getDescription() != null ? certificate.getDescription() : "");

            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(51, 65, 85));
            Paragraph descPara = new Paragraph(descriptionText, bodyFont);
            descPara.setAlignment(Element.ALIGN_CENTER);
            descPara.setLeading(16.5f);
            descPara.setSpacingAfter(22f); // Balanced spacing to raised footer
            document.add(descPara);

            // 6. FOOTER TABLE: 3 EVENLY BALANCED COLUMNS (33% | 34% | 33%)
            PdfPTable footerTable = new PdfPTable(3);
            footerTable.setWidthPercentage(94);
            footerTable.setWidths(new float[]{33, 34, 33});

            Font sigNameFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, new Color(15, 23, 42));
            Font sigRoleFont = FontFactory.getFont(FontFactory.HELVETICA, 9, new Color(100, 116, 139));
            Font qrMetaFont = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(100, 116, 139));

            // --- COLUMN 1: SIGNATORY 1 ---
            PdfPCell sig1Cell = new PdfPCell();
            sig1Cell.setBorder(Rectangle.NO_BORDER);
            sig1Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            sig1Cell.setVerticalAlignment(Element.ALIGN_BOTTOM);

            Image sig1Image = resolveImage(template.getSignatoryOneImage());
            if (sig1Image != null) {
                sig1Image.scaleToFit(110, 36);
                sig1Image.setAlignment(Element.ALIGN_CENTER);
                sig1Cell.addElement(sig1Image);
            } else {
                Paragraph spacer = new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 16));
                spacer.setAlignment(Element.ALIGN_CENTER);
                sig1Cell.addElement(spacer);
            }

            // Compact signature line (~160px width)
            Paragraph sig1Line = new Paragraph("___________________", FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(203, 213, 225)));
            sig1Line.setAlignment(Element.ALIGN_CENTER);
            sig1Line.setSpacingAfter(3f);
            sig1Cell.addElement(sig1Line);

            String sig1Name = template.getSignatoryOneName() != null ? template.getSignatoryOneName() : "";
            Paragraph sig1NamePara = new Paragraph(sig1Name, sigNameFont);
            sig1NamePara.setAlignment(Element.ALIGN_CENTER);
            sig1Cell.addElement(sig1NamePara);

            String sig1Role = template.getSignatoryOneDesignation() != null ? template.getSignatoryOneDesignation() : "";
            Paragraph sig1RolePara = new Paragraph(sig1Role, sigRoleFont);
            sig1RolePara.setAlignment(Element.ALIGN_CENTER);
            sig1Cell.addElement(sig1RolePara);

            footerTable.addCell(sig1Cell);

            // --- COLUMN 2: QR CODE & VERIFICATION (CENTERED & PROPORTIONATE) ---
            PdfPCell qrCell = new PdfPCell();
            qrCell.setBorder(Rectangle.NO_BORDER);
            qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            qrCell.setVerticalAlignment(Element.ALIGN_BOTTOM);

            // Slightly larger QR code (~90px / ~32mm) for optimal scanability and balance
            byte[] qrBytes = qrCodeService.generateQrCodeImageBytes(verificationUrl, 92, 92);
            Image qrImage = Image.getInstance(qrBytes);
            qrImage.scaleToFit(90, 90);
            qrImage.setAlignment(Element.ALIGN_CENTER);
            qrCell.addElement(qrImage);

            Paragraph scanPara = new Paragraph("Scan to Verify", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, new Color(71, 85, 105)));
            scanPara.setAlignment(Element.ALIGN_CENTER);
            scanPara.setSpacingBefore(3f);
            qrCell.addElement(scanPara);

            Paragraph codePara = new Paragraph(certificate.getCertificateNumber(), qrMetaFont);
            codePara.setAlignment(Element.ALIGN_CENTER);
            qrCell.addElement(codePara);

            footerTable.addCell(qrCell);

            // --- COLUMN 3: SIGNATORY 2 ---
            PdfPCell sig2Cell = new PdfPCell();
            sig2Cell.setBorder(Rectangle.NO_BORDER);
            sig2Cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            sig2Cell.setVerticalAlignment(Element.ALIGN_BOTTOM);

            Image sig2Image = resolveImage(template.getSignatoryTwoImage());
            if (sig2Image != null) {
                sig2Image.scaleToFit(110, 36);
                sig2Image.setAlignment(Element.ALIGN_CENTER);
                sig2Cell.addElement(sig2Image);
            } else {
                Paragraph spacer = new Paragraph(" ", FontFactory.getFont(FontFactory.HELVETICA, 16));
                spacer.setAlignment(Element.ALIGN_CENTER);
                sig2Cell.addElement(spacer);
            }

            // Compact signature line (~160px width)
            Paragraph sig2Line = new Paragraph("___________________", FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(203, 213, 225)));
            sig2Line.setAlignment(Element.ALIGN_CENTER);
            sig2Line.setSpacingAfter(3f);
            sig2Cell.addElement(sig2Line);

            String sig2Name = template.getSignatoryTwoName() != null ? template.getSignatoryTwoName() : "";
            Paragraph sig2NamePara = new Paragraph(sig2Name, sigNameFont);
            sig2NamePara.setAlignment(Element.ALIGN_CENTER);
            sig2Cell.addElement(sig2NamePara);

            String sig2Role = template.getSignatoryTwoDesignation() != null ? template.getSignatoryTwoDesignation() : "";
            Paragraph sig2RolePara = new Paragraph(sig2Role, sigRoleFont);
            sig2RolePara.setAlignment(Element.ALIGN_CENTER);
            sig2Cell.addElement(sig2RolePara);

            footerTable.addCell(sig2Cell);

            document.add(footerTable);

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate certificate PDF", e);
        }
    }

    /**
     * Draw modern geometric background accents and border matching reference design.
     */
    private void drawCertificateBackground(PdfContentByte cb, float width, float height) {
        // Base white background
        cb.setColorFill(new Color(255, 255, 255));
        cb.rectangle(0, 0, width, height);
        cb.fill();

        // --- Geometric Accents matching reference design (Left & Bottom-Left) ---
        // Subtle top-left outer bleed accent (confined to outermost bleed margin < 20pt)
        cb.setColorFill(new Color(37, 99, 235)); // Blue 600
        cb.moveTo(0, height);
        cb.lineTo(20, height);
        cb.lineTo(0, height - 20);
        cb.closePath();
        cb.fill();

        // Bottom-left outer polygon (Deep Indigo/Navy)
        cb.setColorFill(new Color(30, 58, 138)); // Blue 900
        cb.moveTo(0, 0);
        cb.lineTo(150, 0);
        cb.lineTo(0, 180);
        cb.closePath();
        cb.fill();

        // Bottom-left middle accent polygon (Slate/Sky)
        cb.setColorFill(new Color(59, 130, 246)); // Blue 500
        cb.moveTo(0, 0);
        cb.lineTo(100, 0);
        cb.lineTo(0, 120);
        cb.closePath();
        cb.fill();

        // Bottom-right subtle accent polygon
        cb.setColorFill(new Color(219, 234, 254)); // Light blue tint
        cb.moveTo(width, 0);
        cb.lineTo(width - 90, 0);
        cb.lineTo(width, 100);
        cb.closePath();
        cb.fill();

        // --- Elegant Certificate Border Frame ---
        // Inner rectangular frame
        cb.setColorStroke(new Color(203, 213, 225)); // Slate 300
        cb.setLineWidth(1.5f);
        cb.rectangle(24, 24, width - 48, height - 48);
        cb.stroke();

        // Secondary subtle inner margin border line
        cb.setColorStroke(new Color(226, 232, 240)); // Slate 200
        cb.setLineWidth(0.75f);
        cb.rectangle(28, 28, width - 56, height - 56);
        cb.stroke();
    }

    /**
     * Resolves an image from a base64 string, classpath resource, or file system path.
     * Returns null gracefully if resolution fails, avoiding broken images.
     */
    private Image resolveImage(String imageSource) {
        if (imageSource == null || imageSource.trim().isEmpty()) {
            return null;
        }

        String src = imageSource.trim();

        try {
            // Case 1: Base64 data URI (e.g. data:image/png;base64,...)
            if (src.startsWith("data:image")) {
                int commaIndex = src.indexOf(",");
                if (commaIndex != -1) {
                    String base64Data = src.substring(commaIndex + 1);
                    byte[] imageBytes = Base64.getDecoder().decode(base64Data);
                    return Image.getInstance(imageBytes);
                }
            }

            // Case 2: Classpath resource
            if (src.startsWith("/")) {
                String cleanPath = src.startsWith("/static/") ? src.substring("/static".length()) : src;
                ClassPathResource resource = new ClassPathResource("static" + cleanPath);
                if (resource.exists()) {
                    try (InputStream is = resource.getInputStream()) {
                        byte[] bytes = is.readAllBytes();
                        return Image.getInstance(bytes);
                    }
                }
            }

            // Case 3: Local file system path
            File file = new File(src);
            if (file.exists() && file.isFile()) {
                return Image.getInstance(file.getAbsolutePath());
            }

            // Case 4: Try directly as resource in classpath
            ClassPathResource directResource = new ClassPathResource(src);
            if (directResource.exists()) {
                try (InputStream is = directResource.getInputStream()) {
                    byte[] bytes = is.readAllBytes();
                    return Image.getInstance(bytes);
                }
            }

        } catch (Exception ignored) {
            // Gracefully ignore and return null so certificate generation never crashes
        }

        return null;
    }
}
