package pt.ist.fenixframework;

import java.net.URL;
import java.util.List;

public interface FenixFrameworkPlugin {

    public List<URL> getDomainModel();

    public void initialize();
}
