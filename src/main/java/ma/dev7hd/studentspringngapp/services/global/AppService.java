package ma.dev7hd.studentspringngapp.services.global;

import lombok.AllArgsConstructor;
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
        Map<String, Map> dashboardData = new HashMap<>();
        dashboardData.put("countStudentsByProgram", iUserService.getProgramIdCounts());
        dashboardData.put("paymentsCountByMonth", iPaymentService.getPaymentsByMonth(null).getBody());
        iNotificationService.pushNotifications();
        return dashboardData;
    }

    @Override
    public Map<String, Map> dashboardData(Integer month){
        Map<String, Map> dashboardData = new HashMap<>();
        dashboardData.put("countStudentsByProgram", iUserService.getProgramIdCounts());
        dashboardData.put("paymentsCountByMonth", iPaymentService.getPaymentsByMonth(month).getBody());
        return dashboardData;
    }

}
