package ma.dev7hd.studentspringngapp.dtos.infoDTOs;

import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayInputStream;

@Getter @Setter
public class InvoiceDTO {
    ByteArrayInputStream stream;
    String number;
}
