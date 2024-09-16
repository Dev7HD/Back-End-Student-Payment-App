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

import java.time.Duration;
import java.time.Instant;
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
        // Start time
        Instant start = Instant.now();

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

        int i = saveStudentsToDatabase(students);

        String message;

        if(i == 0){
            message = "No students were saved.";
        } else if (i == 1) {
            message = "One students were saved.";
        } else {
            message = i + " students were saved.";
        }
        // End time
        Instant end = Instant.now();

        // Calculate the duration
        Duration duration = Duration.between(start, end);

        String timeSpent = getTimeSpent(duration);

        return ResponseEntity.ok(message + " " + timeSpent);
    }

    private static @NotNull String getTimeSpent(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        long millis = duration.toMillisPart();

        String timeSpent;

        if(hours > 0){
            timeSpent = String.format("Time spent: %02d hours %02d minutes %02d seconds and %03d milliseconds", hours, minutes, seconds, millis);
        } else if(minutes > 0){
            timeSpent = String.format("Time spent: %02d minutes %02d seconds and %03d milliseconds", minutes, seconds, millis);
        } else {
            timeSpent = String.format("Time spent: %02d seconds and %03d milliseconds", seconds, millis);
        }
        return timeSpent;
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

    private int saveStudentsToDatabase(List<NewStudentDTO> students) {
        final String defaultPassword = "123456";
        int i = 0;
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
                i++;
            }
        }
        return i;
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
