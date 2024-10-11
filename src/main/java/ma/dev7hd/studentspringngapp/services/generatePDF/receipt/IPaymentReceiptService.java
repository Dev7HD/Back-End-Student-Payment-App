package ma.dev7hd.studentspringngapp.services.generatePDF.receipt;

import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InvoiceDTO;

import java.io.IOException;
import java.util.UUID;

public interface IPaymentReceiptService {
    InvoiceDTO generatePaymentReceipt(UUID paymentId) throws IOException;
}
