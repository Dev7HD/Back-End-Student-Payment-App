package ma.dev7hd.studentspringngapp.metier.dataFromFile;

import jakarta.transaction.Transactional;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface ILoadStudents {
    @Transactional
    ResponseEntity<String> uploadStudentFile(@NotNull MultipartFile file) throws Exception;
}
