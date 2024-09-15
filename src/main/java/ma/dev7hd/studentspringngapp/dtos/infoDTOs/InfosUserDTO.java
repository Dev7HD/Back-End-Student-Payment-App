package ma.dev7hd.studentspringngapp.dtos.infoDTOs;

import lombok.*;

@Getter
@Setter
public class InfosUserDTO {
    private String email;
    private String firstName;
    private String lastName;
    private boolean enabled;
}
