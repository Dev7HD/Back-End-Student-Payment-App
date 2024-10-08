package ma.dev7hd.studentspringngapp.entities.payments;

import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.*;
import ma.dev7hd.studentspringngapp.entities.users.Student;
import ma.dev7hd.studentspringngapp.entities.users.User;
import ma.dev7hd.studentspringngapp.enumirat.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false, updatable = false)
    private Date date;

    @Column(nullable = false, updatable = false)
    private Date registerDate;

    @Column(nullable = false, updatable = false)
    @Positive(message = "Amount must be greater then 0")
    private double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private PaymentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @Column(nullable = false, updatable = false)
    private String receipt;

    private String invoiceNumber;

    private LocalDateTime invoiceDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "student_id", nullable = false, updatable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "added_by", nullable = false, updatable = false)
    private User addedBy;

    @OneToMany(mappedBy = "payment", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PaymentStatusChange> paymentStatusChanges;

}
