package ma.dev7hd.studentspringngapp.entities.users;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import ma.dev7hd.studentspringngapp.entities.registrations.BanedRegistration;
import ma.dev7hd.studentspringngapp.entities.notifications.admins.Notification;
import ma.dev7hd.studentspringngapp.entities.payments.PaymentStatusChange;
import ma.dev7hd.studentspringngapp.enumirat.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
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

    @ManyToMany(mappedBy = "adminRemover")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Notification> deletedNotifications;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_STUDENT"), new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

}
