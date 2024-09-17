package ma.dev7hd.studentspringngapp.metier.global;

import org.springframework.data.crossstore.ChangeSetPersister;

import java.util.Map;

public interface IGlobalMetier {
    Map<String, Map> onLoginFetchData() throws ChangeSetPersister.NotFoundException;

    Map<String, Map> dashboardData(Integer month);

}
