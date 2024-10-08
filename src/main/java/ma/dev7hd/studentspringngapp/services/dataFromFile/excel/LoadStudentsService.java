package ma.dev7hd.studentspringngapp.services.dataFromFile.excel;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.dtos.infoDTOs.InfosStudentDTO;
import ma.dev7hd.studentspringngapp.entities.users.Student;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import ma.dev7hd.studentspringngapp.repositories.users.StudentRepository;
import ma.dev7hd.studentspringngapp.services.global.IUserDataProvider;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class LoadStudentsService implements ILoadStudentsService {
    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final IUserDataProvider userDataProvider;

    private final String DEFAULT_PASSWORD = "123456";
    private final List<String> DOC_HEADER = List.of("email", "firstName", "lastName", "programId");
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Transactional
    @Override
    public ResponseEntity<String> uploadStudentFile(@NotNull MultipartFile file) throws Exception {
        // Start time
        Instant start = Instant.now();

        AtomicReference<Integer> maxCodeValue = new AtomicReference<>(studentRepository.findLastIdentifierByProgram().orElse(1000));

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

        List<InfosStudentDTO> students = new ArrayList<>();
        List<Integer> errorRowIndexes = new ArrayList<>();

        // Create tasks for processing rows in parallel using CompletableFuture
        List<CompletableFuture<InfosStudentDTO>> futures = IntStream.range(1, sheet.getLastRowNum() + 1)
                .mapToObj(i -> CompletableFuture.supplyAsync(() -> {
                    Row row = sheet.getRow(i);
                    maxCodeValue.updateAndGet(v -> v + 1);
                    if (!validateRow(row)) {
                        errorRowIndexes.add(row.getRowNum());
                        return null;
                    }
                    return processExcelRow(row, maxCodeValue.get());
                }, executorService))
                .toList();

        // Collect results from the futures
        for (CompletableFuture<InfosStudentDTO> future : futures) {
            InfosStudentDTO studentDTO = future.get(); // This will wait for each thread to complete
            if (studentDTO != null) {
                students.add(studentDTO);
            }
        }

        if (students.isEmpty()) {
            return ResponseEntity.badRequest().body("No students found.");
        }

        Map<String, Integer> statistics = saveStudentsToDatabaseParallel(students);

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

    private Map<String, Integer> saveStudentsToDatabaseParallel(List<InfosStudentDTO> students) throws InterruptedException, ExecutionException {
        Set<String> allEmails = studentRepository.findAllEmails();

        // Create a list to hold the future results
        List<CompletableFuture<Student>> futures = students.stream()
                .filter(dto -> !allEmails.contains(dto.getEmail()) )
                .map(dto -> CompletableFuture.supplyAsync(() -> {
                    Student student = new Student();
                    student.setEmail(dto.getEmail());
                    student.setFirstName(dto.getFirstName());
                    student.setLastName(dto.getLastName());
                    student.setProgramId(dto.getProgramId());
                    student.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
                    student.setPasswordChanged(false);
                    student.setCode(dto.getCode());
                    Student.updateProgramCountsFromDB(student.getProgramId(), 1.0);
                    return student;
                }, executorService))
                .toList();

        // Wait for all the tasks to finish and collect the results
        List<Student> studentEntities = futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());

        if (!studentEntities.isEmpty()) {
            studentRepository.saveAll(studentEntities);  // Batch save
        }

        Map<String, Integer> statistics = new HashMap<>();
        statistics.put("saved", studentEntities.size());
        statistics.put("existed", (students.size() - studentEntities.size()));

        return statistics;
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

    private String getRowErrorMessage(List<Integer> errorRowIndexes) {
        if (errorRowIndexes.size() > 20) {
            return errorRowIndexes.size() + " Excel rows have invalid data and weren't saved.";
        } else if (errorRowIndexes.size() > 1) {
            return "Due to invalid data, " + errorRowIndexes.size() + " records weren't saved. Rows: " +
                    errorRowIndexes.stream().map(String::valueOf).collect(Collectors.joining(", "));
        } else if (errorRowIndexes.size() == 1) {
            return "Due to invalid data, one record wasn't saved. Row: " + errorRowIndexes.get(0);
        }
        return "";
    }

    private InfosStudentDTO processExcelRow(Row row, Integer maxCodeValue) {
        try {
            InfosStudentDTO studentDTO = new InfosStudentDTO();
            studentDTO.setEmail(row.getCell(0).getStringCellValue());
            studentDTO.setFirstName(row.getCell(1).getStringCellValue());
            studentDTO.setLastName(row.getCell(2).getStringCellValue());
            studentDTO.setProgramId(ProgramID.valueOf(row.getCell(3).getStringCellValue()));
            studentDTO.setCode(maxCodeValue.toString());
            studentDTO.setEnabled(true);
            String studentCode = userDataProvider.generateStudentCode(studentDTO.getProgramId(), maxCodeValue);
            studentDTO.setCode(studentCode);
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
                DOC_HEADER.get(3).equalsIgnoreCase(headerRow.getCell(3).getStringCellValue());
    }

    private boolean validateRow(Row row) {
        try {
            if (row.getCell(0).getCellType() != CellType.STRING && row.getCell(0).getStringCellValue().matches("[a-z]{4}[a-z0-9._%+-]+@[a-z]{2,3}[a-z0-9.-]+\\.[a-z]{2,3}")) return false; //Email
            if (row.getCell(1).getCellType() != CellType.STRING) return false; //FirstName
            if (row.getCell(2).getCellType() != CellType.STRING) return false; //LastName
            if (row.getCell(3).getCellType() != CellType.STRING || ProgramID.valueOf(row.getCell(3).getStringCellValue()) == null) return false; // ProgramID
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }

    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
