package com.yeab.ticketing.payment.enums;

public enum InvoiceStatus {
    /** Invoice issued, payment not yet received. */
    ISSUED,
    /** Payment received — invoice is settled. */
    PAID,
    /** Payment failed or cancelled — invoice is void. */
    VOID
}
