package ma.dev7hd.studentspringngapp.services.dataFromFile.excel;

import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ILoadStudentsService {
    @Transactional
    ResponseEntity<String> uploadStudentFile(@NotNull MultipartFile file) throws Exception;
}
