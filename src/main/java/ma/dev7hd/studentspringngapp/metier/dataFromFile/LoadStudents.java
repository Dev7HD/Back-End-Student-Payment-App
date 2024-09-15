package ma.dev7hd.studentspringngapp.metier.dataFromFile;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.newObjectDTOs.NewStudentDTO;
import ma.dev7hd.studentspringngapp.entities.Student;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import ma.dev7hd.studentspringngapp.repositories.StudentRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class LoadStudents implements ILoadStudents {
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    private final List<String> docHeaders = List.of("email", "firstName", "lastName", "code", "programId");

    @Transactional
    @Override
    public ResponseEntity<String> uploadStudentFile(@NotNull MultipartFile file) throws Exception {
        String fileType = getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));

        List<NewStudentDTO> students;

        if ("xls".equalsIgnoreCase(fileType) || "xlsx".equalsIgnoreCase(fileType)) {
            Workbook workbook = new XSSFWorkbook(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            // Validate headers
            Row headerRow = sheet.getRow(0);
            if (!validateHeaders(headerRow)) {
                return ResponseEntity.badRequest().body("Invalid table headers.");
            }
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);

                if (!validateRow(row)) {
                    return ResponseEntity.badRequest().body("Invalid data types in Excel rows.");
                }
            }


            students = processExcelFile(file);
        } else {
            return ResponseEntity.badRequest().body("File type not accepted.");
        }

        if (students.isEmpty()) {
            return ResponseEntity.badRequest().body("No students found.");
        }

        saveStudentsToDatabase(students);
        return ResponseEntity.ok("All students are saved.");
    }

    private List<NewStudentDTO> processExcelFile(MultipartFile file) throws Exception {
        List<NewStudentDTO> students = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);

                NewStudentDTO studentDTO = new NewStudentDTO();
                studentDTO.setEmail(row.getCell(0).getStringCellValue());
                studentDTO.setFirstName(row.getCell(1).getStringCellValue());
                studentDTO.setLastName(row.getCell(2).getStringCellValue());
                studentDTO.setCode(row.getCell(3).getStringCellValue());
                studentDTO.setProgramId(ProgramID.valueOf(row.getCell(4).getStringCellValue()));

                students.add(studentDTO);
            }
        }

        return students;
    }

    private boolean validateHeaders(Row headerRow) {
        return headerRow != null &&
                docHeaders.get(0).equalsIgnoreCase(headerRow.getCell(0).getStringCellValue()) &&
                docHeaders.get(1).equalsIgnoreCase(headerRow.getCell(1).getStringCellValue()) &&
                docHeaders.get(2).equalsIgnoreCase(headerRow.getCell(2).getStringCellValue()) &&
                docHeaders.get(3).equalsIgnoreCase(headerRow.getCell(3).getStringCellValue()) &&
                docHeaders.get(4).equalsIgnoreCase(headerRow.getCell(4).getStringCellValue());
    }

    private boolean validateRow(Row row) {
        try {
            if (row.getCell(0).getCellType() != CellType.STRING) return false; //Email
            if (row.getCell(1).getCellType() != CellType.STRING) return false; //FirstName
            if (row.getCell(2).getCellType() != CellType.STRING) return false; //LastName
            if (row.getCell(3).getCellType() != CellType.STRING) return false; //Code
            if (row.getCell(4).getCellType() != CellType.STRING || ProgramID.valueOf(row.getCell(4).getStringCellValue()) == null) return false; // ProgramID
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void saveStudentsToDatabase(List<NewStudentDTO> students) {
        final String defaultPassword = "123456";
        for (NewStudentDTO dto : students) {
            if(studentRepository.existsByEmailOrCode(dto.getEmail(), dto.getCode())) {
                throw new RuntimeException("Email or student code already exists");
            } else {
                Student student = new Student();
                student.setEmail(dto.getEmail());
                student.setFirstName(dto.getFirstName());
                student.setLastName(dto.getLastName());
                student.setCode(dto.getCode());
                student.setProgramId(dto.getProgramId());
                student.setPasswordChanged(false);
                student.setEnabled(true);
                student.setPassword(passwordEncoder.encode(defaultPassword));

                studentRepository.save(student);
            }
        }
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
