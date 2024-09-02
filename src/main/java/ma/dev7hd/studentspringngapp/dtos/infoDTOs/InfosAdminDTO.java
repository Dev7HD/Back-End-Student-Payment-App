package ma.dev7hd.studentspringngapp.dtos.infoDTOs;

import lombok.Getter;
import lombok.Setter;
import ma.dev7hd.studentspringngapp.enumirat.DepartmentName;

@Getter
@Setter
public class InfosAdminDTO extends InfosUserDTO {
    private DepartmentName departmentName;
}
