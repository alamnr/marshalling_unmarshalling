package info.ejava.examples.svc.aop.items.introductions;

import java.util.List;

/**
 * This is an example Introduction interface that will be assigned to
 * a proxy that wraps an advised object.
 * The interface is used to track the callers of the target object.
 */
public interface MyUsageIntroduction {
    List<String> getAllCalled();
    void addCalled(String called);
    void clear();
}
