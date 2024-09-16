package ma.dev7hd.studentspringngapp.web;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.metier.global.IGlobalMetier;
import ma.dev7hd.studentspringngapp.services.IPaymentService;
import ma.dev7hd.studentspringngapp.services.IUserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping("/app")
public class GlobalRestController {
    private final IGlobalMetier globalMetier;
    private final IUserService iUserService;
    private final IPaymentService iPaymentService;

    @GetMapping("/on-login-data")
    public Map<String, Map> onLoginFetchData(){
        return globalMetier.onLoginFetchData();
    }

    @GetMapping("/dashboard-data")
    public Map<String, Map> dashboardData(@RequestParam(required = false) Integer month){
        return globalMetier.dashboardData(month);
    }

    @PostMapping("/notifications/mark-all-as-read")
    public void markAllAsRead() {
        iUserService.markAsReadAllPendingStudents();
        iPaymentService.markAllAsRead();
    }
}
