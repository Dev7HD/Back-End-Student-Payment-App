package ma.dev7hd.studentspringngapp.metier.global;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.services.IPaymentService;
import ma.dev7hd.studentspringngapp.services.IUserService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@AllArgsConstructor
public class GlobalMetier implements IGlobalMetier{
    private final IUserService iUserService;
    private final IPaymentService iPaymentService;

    @Override
    public Map<String, Map> onLoginFetchData(){
        Map<String, Map> dashboardData = new HashMap<>();
        dashboardData.put("countStudentsByProgram", iUserService.getProgramIdCounts());
        dashboardData.put("paymentsCountByMonth", iPaymentService.getPaymentsByMonth(null).getBody());
        iUserService.onLoginNotifications();
        iPaymentService.onLoginPaymentNotifications();
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
