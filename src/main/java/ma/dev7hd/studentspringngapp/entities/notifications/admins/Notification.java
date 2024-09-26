package ma.dev7hd.studentspringngapp.entities.notifications.admins;

import jakarta.persistence.*;
import lombok.*;
import ma.dev7hd.studentspringngapp.entities.users.Admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;
    private String message;
    private boolean seen;
    private Date registerDate;

    @ManyToMany(fetch = FetchType.EAGER)
    private List<Admin> adminRemover  = new ArrayList<>();
}
