package ma.dev7hd.studentspringngapp.metier.global;

import java.util.Map;

public interface IGlobalMetier {
    Map<String, Map> onLoginFetchData();

    Map<String, Map> dashboardData(Integer month);
}
