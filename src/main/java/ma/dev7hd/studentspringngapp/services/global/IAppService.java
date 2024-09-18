package ma.dev7hd.studentspringngapp.services.global;

import org.springframework.data.crossstore.ChangeSetPersister;

import java.util.Map;

public interface IAppService {
    Map<String, Map> onLoginFetchData() throws ChangeSetPersister.NotFoundException;

    Map<String, Map> dashboardData(Integer month);

}
