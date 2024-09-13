package ma.dev7hd.studentspringngapp.dtos.infoDTOs;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
public class PendingUserDTO {
    String email;
    String firstName;
    String lastName;
    Date registerDate;
    String message;
    boolean seen;
}
