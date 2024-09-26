package ma.dev7hd.studentspringngapp.entities.payments;

import jakarta.persistence.*;
import lombok.*;
import ma.dev7hd.studentspringngapp.entities.users.Admin;
import ma.dev7hd.studentspringngapp.enumirat.PaymentStatus;

import java.util.Date;
import java.util.UUID;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class PaymentStatusChange {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "admin_id", nullable = false, updatable = false)
    private Admin admin;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_id", nullable = false, updatable = false)
    private Payment payment;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date changeDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private PaymentStatus newStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private PaymentStatus oldStatus;
}

