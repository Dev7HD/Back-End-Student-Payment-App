package ma.dev7hd.studentspringngapp.repositories;

import ma.dev7hd.studentspringngapp.entities.Admin;
import ma.dev7hd.studentspringngapp.enumirat.DepartmentName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminRepository extends JpaRepository<Admin, String> {
    @Query("SELECT a FROM Admin a WHERE " +
            "(:email IS NULL OR a.email LIKE %:email%) AND " +
            "(:firstName IS NULL OR a.firstName LIKE %:firstName%) AND " +
            "(:lastName IS NULL OR a.lastName LIKE %:lastName%) AND " +
            "(:departmentName IS NULL OR a.departmentName = :departmentName)")
    Page<Admin> findByFilter(
            @Param("email") String email,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("departmentName") DepartmentName departmentName,
            Pageable pageable);
}
