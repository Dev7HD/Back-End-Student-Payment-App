package ma.dev7hd.studentspringngapp.services.global;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.entities.users.Admin;
import ma.dev7hd.studentspringngapp.entities.users.Student;
import ma.dev7hd.studentspringngapp.entities.users.User;
import ma.dev7hd.studentspringngapp.enumirat.Months;
import ma.dev7hd.studentspringngapp.enumirat.ProgramID;
import ma.dev7hd.studentspringngapp.repositories.payments.PaymentRepository;
import ma.dev7hd.studentspringngapp.services.notification.INotificationService;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class AppService implements IAppService {
    private final INotificationService iNotificationService;
    private final PaymentRepository paymentRepository;
    private final IUserDataProvider iUserDataProvider;


    @Override
    public Map<String, Object> onLoginFetchData() throws ChangeSetPersister.NotFoundException {
        String currentUserEmail = iUserDataProvider.getCurrentUserEmail();
        User user = iUserDataProvider.getUserByEmail(currentUserEmail);
        Map<String, Object> data = new HashMap<>();
        if (user != null) {
            if(user instanceof Student) {
                data.put("NonSeenNotificationsCount", iNotificationService.getStudentNotificationsNonSeenCount());
                iNotificationService.getStudentNotifications(currentUserEmail);
                return data;
            }
            else if (user instanceof Admin){

                data.put("countStudentsByProgram", getProgramIdCounts());
                data.put("paymentsCountByMonth", getPaymentsByMonth(null).getBody());
                data.put("NonSeenNotificationsCount", iNotificationService.getAdminNotificationsNonSeenCount());
                iNotificationService.pushAdminNotifications();
                return data;
            }
        }
        return null;
    }

    @Override
    public Map<String, Map> dashboardData(Integer month){
        Map<String, Map> dashboardData = new HashMap<>();
        dashboardData.put("countStudentsByProgram", getProgramIdCounts());
        dashboardData.put("paymentsCountByMonth", getPaymentsByMonth(month).getBody());
        return dashboardData;
    }

    private Map<ProgramID, List<Double>> getProgramIdCounts(){
        return Student.programIDCounter;
    }

    @Override
    public ResponseEntity<Map<Months, Long>> getPaymentsByMonth(Integer month) {
        if(month != null && (month > 12 || month < 1)) {
            return ResponseEntity.badRequest().build();
        }

        Map<Months, Long> countByMonth = new EnumMap<>(Months.class);
        if(month == null){
            List<Long[]> counted = paymentRepository.countAllPaymentsGroupByDateAndOptionalMonth(month);
            int i = 0;
            for(Months months : Months.values()) {
                countByMonth.put(months, counted.get(i)[1]);
                i++;
            }
        } else {
            Long[] counted = paymentRepository.countPaymentsByMonth(month);
            countByMonth.put(Months.values()[month - 1], counted[0]);
        }
        return ResponseEntity.ok(countByMonth);
    }
}
