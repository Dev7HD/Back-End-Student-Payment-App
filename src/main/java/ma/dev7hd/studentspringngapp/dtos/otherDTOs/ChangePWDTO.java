package ma.dev7hd.studentspringngapp.dtos.otherDTOs;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChangePWDTO {
    private String oldPassword;
    private String newPassword;
}
