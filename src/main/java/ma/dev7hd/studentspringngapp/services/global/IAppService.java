package ma.dev7hd.studentspringngapp.services.global;

import ma.dev7hd.studentspringngapp.enumirat.Months;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface IAppService {
    Map<String, Object> onLoginFetchData() throws ChangeSetPersister.NotFoundException;

    Map<String, Map> dashboardData(Integer month);

    ResponseEntity<Map<Months, Long>> getPaymentsByMonth(Integer month);
}
