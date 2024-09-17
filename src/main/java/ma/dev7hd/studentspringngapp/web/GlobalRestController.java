package ma.dev7hd.studentspringngapp.web;

import lombok.AllArgsConstructor;
import ma.dev7hd.studentspringngapp.metier.global.IGlobalMetier;
import ma.dev7hd.studentspringngapp.services.IPaymentService;
import ma.dev7hd.studentspringngapp.services.IUserService;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin("*")
@AllArgsConstructor
@RequestMapping("/app")
public class GlobalRestController {
    private final IGlobalMetier globalMetier;

    @GetMapping("/on-login-data")
    public Map<String, Map> onLoginFetchData() throws ChangeSetPersister.NotFoundException {
        return globalMetier.onLoginFetchData();
    }

    @GetMapping("/dashboard-data")
    public Map<String, Map> dashboardData(@RequestParam(required = false) Integer month){
        return globalMetier.dashboardData(month);
    }

}
