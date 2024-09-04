package ma.dev7hd.studentspringngapp.dtos.infoDTOs;


import lombok.*;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;

@Getter
@Setter
public class InfosStudentDTO extends InfosUserDTO {
    private String code;
    private ProgramID programId;
}
