package okapi.tmserver;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

public class OkapiTmServerApp extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(RestServicesImpl.class);
        return classes;
    }
}

