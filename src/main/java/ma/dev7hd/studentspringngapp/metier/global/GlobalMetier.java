package ma.dev7hd.studentspringngapp.metier.global;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.metier.notification.INotificationMetier;
import ma.dev7hd.studentspringngapp.services.IPaymentService;
import ma.dev7hd.studentspringngapp.services.IUserService;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@AllArgsConstructor
public class GlobalMetier implements IGlobalMetier{
    private final IUserService iUserService;
    private final IPaymentService iPaymentService;
    private final INotificationMetier iNotificationMetier;

    @Override
    public Map<String, Map> onLoginFetchData() throws ChangeSetPersister.NotFoundException {
        Map<String, Map> dashboardData = new HashMap<>();
        dashboardData.put("countStudentsByProgram", iUserService.getProgramIdCounts());
        dashboardData.put("paymentsCountByMonth", iPaymentService.getPaymentsByMonth(null).getBody());
        iNotificationMetier.pushNotifications();
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
