package ma.dev7hd.studentspringngapp.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

@Entity
@DiscriminatorValue("STUDENT")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Student extends User {

    private String code;

    @Enumerated(EnumType.STRING)
    private ProgramID programId;

    @OneToMany(mappedBy = "student", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<Payment> payments;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_STUDENT"));
    }

    public static Map<ProgramID, List<Double>> programIDCounter = new EnumMap<>(ProgramID.class);

    public static Integer totalStudents = 0;

    public static void updateProgramCountsFromDB(ProgramID programID, Double differenceValue) {
        totalStudents += differenceValue.intValue();

        List<Double> values = programIDCounter.getOrDefault(programID, new ArrayList<>(Arrays.asList(0.0, 0.0)));

        double updatedTotal = values.get(0) + differenceValue;
        values.set(0, updatedTotal);

        double updatedPercentage = updatedTotal / totalStudents;
        values.set(1, updatedPercentage);

        programIDCounter.put(programID, values);

        for (ProgramID otherProgramID : ProgramID.values()) {
            if (!programID.equals(otherProgramID)) {
                programIDCounter.computeIfPresent(otherProgramID, (k, valuesList) -> {
                    double totalForOtherProgram = valuesList.get(0);
                    valuesList.set(1, totalForOtherProgram / totalStudents);
                    return valuesList;
                });
            }
        }
    }
}
