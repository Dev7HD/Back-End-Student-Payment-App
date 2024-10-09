package ma.dev7hd.studentspringngapp.dtos.newObjectDTOs;

import lombok.Getter;
import lombok.Setter;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;

@Getter @Setter
public class NewPendingStudentDTO {
    String email;
    String firstName;
    String lastName;
    ProgramID programID;
}
