package ma.dev7hd.studentspringngapp.services.dataFromFile;

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
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class LoadStudentsService implements ILoadStudentsService {
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    private final String DEFAULT_PASSWORD = "123456";
    private final List<String> DOC_HEADER = List.of("email", "firstName", "lastName", "code", "programId");

    @Transactional
    @Override
    public ResponseEntity<String> uploadStudentFile(@NotNull MultipartFile file) throws Exception {
        // Start time
        Instant start = Instant.now();

        String fileType = getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));

        if (!"xls".equalsIgnoreCase(fileType) && !"xlsx".equalsIgnoreCase(fileType)) {
            return ResponseEntity.badRequest().body("File type not accepted.");
        }

        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        Sheet sheet = workbook.getSheetAt(0);

        // Validate headers
        if (!validateHeaders(sheet.getRow(0))) {
            return ResponseEntity.badRequest().body("Invalid table headers.");
        }

        List<NewStudentDTO> students;
        List<Integer> errorRowIndexes = new ArrayList<>();

        // Process Excel rows in parallel
        students = IntStream.range(1, sheet.getLastRowNum() + 1)
                .mapToObj(sheet::getRow)
                .filter(row -> {
                    if (!validateRow(row)) {
                        errorRowIndexes.add(row.getRowNum());
                        return false;
                    }
                    return true;
                })
                .map(this::processExcelRow)
                .collect(Collectors.toList());

        if (students.isEmpty()) {
            return ResponseEntity.badRequest().body("No students found.");
        }

        Map<String, Integer> statistics = saveStudentsToDatabase(students);

        String savedMessage;
        if (statistics.get("saved") == 0) {
            savedMessage = "No student records were saved.\n";
        } else if (statistics.get("saved") == 1) {
            savedMessage = "Successfully saved one student record in";
        } else {
            savedMessage = "Successfully saved " + statistics.get("saved") + " student records in";
        }

        String existedMessage = "";
        if (statistics.get("existed") == 1){
            existedMessage = statistics.get("existed") + " email/code already existed.\n";
        } else if (statistics.get("existed") > 1) {
            existedMessage = statistics.get("existed") + " emails/codes already existed.\n";
        }

        String rowErrorMessage = getRowErrorMessage(errorRowIndexes);

        // End time
        Instant end = Instant.now();

        String timeSpent = "";

        if(statistics.get("saved") > 0){
            timeSpent = getTimeSpent(Duration.between(start, end)) + ".\n";
        }

        return ResponseEntity.ok(savedMessage + " " + timeSpent + existedMessage + rowErrorMessage);
    }

    private static String getTimeSpent(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        long millis = duration.toMillisPart();

        if (hours > 0) {
            return String.format("%02d hours %02d minutes %02d seconds and %03d milliseconds", hours, minutes, seconds, millis);
        } else if (minutes > 0) {
            return String.format("%02d minutes %02d seconds and %03d milliseconds", minutes, seconds, millis);
        } else {
            return String.format("%02d seconds and %03d milliseconds", seconds, millis);
        }
    }

    private String getRowErrorMessage(List<Integer> errorRowIndexs) {
        if (errorRowIndexs.size() > 20) {
            return errorRowIndexs.size() + " Excel rows have invalid data and weren't saved.";
        } else if (errorRowIndexs.size() > 1) {
            return "Due to invalid data, " + errorRowIndexs.size() + " records weren't saved. Rows: " +
                    errorRowIndexs.stream().map(String::valueOf).collect(Collectors.joining(", "));
        } else if (errorRowIndexs.size() == 1) {
            return "Due to invalid data, one record wasn't saved. Row: " + errorRowIndexs.get(0);
        }
        return "";
    }

    private NewStudentDTO processExcelRow(Row row) {
        try {
            NewStudentDTO studentDTO = new NewStudentDTO();
            studentDTO.setEmail(row.getCell(0).getStringCellValue());
            studentDTO.setFirstName(row.getCell(1).getStringCellValue());
            studentDTO.setLastName(row.getCell(2).getStringCellValue());
            studentDTO.setCode(row.getCell(3).getStringCellValue());
            studentDTO.setProgramId(ProgramID.valueOf(row.getCell(4).getStringCellValue()));
            return studentDTO;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean validateHeaders(Row headerRow) {
        return headerRow != null &&
                DOC_HEADER.get(0).equalsIgnoreCase(headerRow.getCell(0).getStringCellValue()) &&
                DOC_HEADER.get(1).equalsIgnoreCase(headerRow.getCell(1).getStringCellValue()) &&
                DOC_HEADER.get(2).equalsIgnoreCase(headerRow.getCell(2).getStringCellValue()) &&
                DOC_HEADER.get(3).equalsIgnoreCase(headerRow.getCell(3).getStringCellValue()) &&
                DOC_HEADER.get(4).equalsIgnoreCase(headerRow.getCell(4).getStringCellValue());
    }

    private boolean validateRow(Row row) {
        try {
            if (row.getCell(0).getCellType() != CellType.STRING && row.getCell(0).getStringCellValue().matches("[a-z]{4}[a-z0-9._%+-]+@[a-z]{2,3}[a-z0-9.-]+\\.[a-z]{2,3}")) return false; //Email
            if (row.getCell(1).getCellType() != CellType.STRING) return false; //FirstName
            if (row.getCell(2).getCellType() != CellType.STRING) return false; //LastName
            if (row.getCell(3).getCellType() != CellType.STRING && row.getCell(3).getStringCellValue().matches("[a-z]{1,2}[0-9]{4,8}+[a-z]?")) return false; //Code
            if (row.getCell(4).getCellType() != CellType.STRING || ProgramID.valueOf(row.getCell(4).getStringCellValue()) == null) return false; // ProgramID
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private Map<String, Integer> saveStudentsToDatabase(List<NewStudentDTO> students) {
        Set<String> existingEmailsAndCodes = studentRepository.findAllEmailsAndCodes();

        List<Student> studentEntities = students.stream()
                .filter(dto -> !existingEmailsAndCodes.toString().contains(dto.getEmail()) && !existingEmailsAndCodes.toString().contains(dto.getCode()))
                .map(dto -> {
                    Student student = new Student();
                    student.setEmail(dto.getEmail());
                    student.setFirstName(dto.getFirstName());
                    student.setLastName(dto.getLastName());
                    student.setCode(dto.getCode());
                    student.setProgramId(dto.getProgramId());
                    student.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
                    student.setPasswordChanged(false);
                    student.setEnabled(true);
                    Student.updateProgramCountsFromDB(student.getProgramId(),1.0);
                    return student;
                }).collect(Collectors.toList());

        if (!studentEntities.isEmpty()) {
            studentRepository.saveAll(studentEntities);  // Batch save
        }

        Map<String, Integer> statistics = new HashMap<>();

        statistics.put("saved", studentEntities.size());
        statistics.put("existed", (students.size() - studentEntities.size()));

        return statistics;
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
