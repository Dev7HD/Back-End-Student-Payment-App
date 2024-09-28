package ma.dev7hd.studentspringngapp.services.global;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.entities.users.Admin;
import ma.dev7hd.studentspringngapp.entities.users.Student;
import ma.dev7hd.studentspringngapp.entities.users.User;
import ma.dev7hd.studentspringngapp.services.notification.INotificationService;
import ma.dev7hd.studentspringngapp.services.payment.IPaymentService;
import ma.dev7hd.studentspringngapp.services.user.IUserService;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class AppService implements IAppService {
    private final IUserService iUserService;
    private final IPaymentService iPaymentService;
    private final INotificationService iNotificationService;

    @Override
    public Map<String, Map> onLoginFetchData() throws ChangeSetPersister.NotFoundException {
        String currentUserEmail = iUserService.getCurrentUserEmail();
        User user = iUserService.getUserByEmail(currentUserEmail);
        if (user != null) {
            if(user instanceof Student) {
                iNotificationService.getStudentNotifications(currentUserEmail);
                return null;
            }
            else if (user instanceof Admin){
                Map<String, Map> dashboardData = new HashMap<>();
                dashboardData.put("countStudentsByProgram", iUserService.getProgramIdCounts());
                dashboardData.put("paymentsCountByMonth", iPaymentService.getPaymentsByMonth(null).getBody());
                iNotificationService.pushAdminNotifications();
                return dashboardData;
            }
        }
        return null;
    }

    @Override
    public Map<String, Map> dashboardData(Integer month){
        Map<String, Map> dashboardData = new HashMap<>();
        dashboardData.put("countStudentsByProgram", iUserService.getProgramIdCounts());
        dashboardData.put("paymentsCountByMonth", iPaymentService.getPaymentsByMonth(month).getBody());
        return dashboardData;
    }

}
