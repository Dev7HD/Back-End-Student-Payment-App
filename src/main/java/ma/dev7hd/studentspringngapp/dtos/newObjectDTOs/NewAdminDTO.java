package ma.dev7hd.studentspringngapp.dtos.newObjectDTOs;

import lombok.Getter;
import lombok.Setter;
import ma.dev7hd.studentspringngapp.enumirat.DepartmentName;

@Getter
@Setter
public class NewAdminDTO extends NewUserDTO {
    private DepartmentName departmentName;
}
