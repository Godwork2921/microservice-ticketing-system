package com.yeab.ticketing.ticket.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Generates a QR PNG for a ticket. The payload is a compact JSON fragment
 * carrying the ticket number; scanners pass it unmodified to
 * {@code POST /api/tickets/validate}.
 */
@Component
public class QrCodeService {

    private static final int SIZE = 300;

    public byte[] generatePng(String ticketCode) {
        if (!StringUtils.hasText(ticketCode)) {
            throw new IllegalArgumentException("ticketCode is required to generate a QR code");
        }
        try {
            String payload = "{\"t\":\"" + ticketCode + "\"}";
            BitMatrix matrix = new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE,
                    SIZE, SIZE, java.util.Map.of(
                            com.google.zxing.EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M,
                            com.google.zxing.EncodeHintType.CHARACTER_SET, "UTF-8"));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out,
                    new MatrixToImageConfig(0xFF000000, 0xFFFFFFFF));
            return out.toByteArray();
        } catch (WriterException | IOException ex) {
            throw new IllegalStateException("Failed to render QR code for ticket " + ticketCode, ex);
        }
    }
}