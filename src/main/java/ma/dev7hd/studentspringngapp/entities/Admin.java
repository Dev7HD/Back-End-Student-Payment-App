package ma.dev7hd.studentspringngapp.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import ma.dev7hd.studentspringngapp.enumirat.*;

import java.util.List;

@Entity
@DiscriminatorValue("ADMIN")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Admin extends User {

    @Enumerated(EnumType.STRING)
    private DepartmentName departmentName;

    @OneToMany(mappedBy = "admin", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<PaymentStatusChange> paymentStatusChanges;

    @OneToMany(mappedBy = "adminBanner", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<BanedRegistration> banedRegistrations;
}
