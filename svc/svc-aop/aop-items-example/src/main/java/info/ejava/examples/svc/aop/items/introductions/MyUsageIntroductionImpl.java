package info.ejava.examples.svc.aop.items.introductions;

import java.util.ArrayList;
import java.util.List;

/**
 * This example Introduction can be added to an advised object to
 * track some per-advised information. The proxy wrapping the advised
 * target will implement the MyUsageIntroduction interface and delegate
 * calls to this class.
 */
public class MyUsageIntroductionImpl implements MyUsageIntroduction {
    private List<String> calls = new ArrayList<>();

    @Override
    public List<String> getAllCalled() {
        return new ArrayList<>(calls);
    }

    @Override
    public void addCalled(String called) {
        calls.add(called);
    }

    @Override
    public void clear() {
        calls.clear();
    }
}
