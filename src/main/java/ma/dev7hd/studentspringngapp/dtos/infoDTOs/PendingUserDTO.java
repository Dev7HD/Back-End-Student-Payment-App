package ma.dev7hd.studentspringngapp.dtos.infoDTOs;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter @Setter
public class PendingUserDTO {
    private String email;
    private String firstName;
    private String lastName;
    private Date registerDate;
    private boolean seen;
    private String message;
}
