package info.ejava.examples.svc.aop.items.proxyfactory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * This is an implementation class for the example Access Introduction.
 * It provides a list of roles the caller has access to for the target
 * object this is added to.
 * @param <T>
 */
@RequiredArgsConstructor
@Getter
@ToString
public class MyAccessIntroductionImpl<T> implements MyAccessIntroduction<T> {
    private final List<Access> userRoles = new ArrayList<>();
    private final T data;

    @Override
    public void setUserRoles(List<Access> roles) {
        this.userRoles.clear();
        this.userRoles.addAll(roles);
    }

    @Override
    public TargetWrapper<T> getRawTarget() {
        //hide the raw data from the proxy so that it does not
        return new TargetWrapper<>(this.data);
    }
}
