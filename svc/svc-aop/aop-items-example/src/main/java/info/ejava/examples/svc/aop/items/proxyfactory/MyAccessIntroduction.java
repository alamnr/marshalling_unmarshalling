package info.ejava.examples.svc.aop.items.proxyfactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * This is an example AOP Introduction interface that will be added to
 * a targeted object at runtime. This is an interface and methods that the
 * target object does not have at compile-time.
 * <p>
 * Its purpose is to identify which roles the attributed user has for
 * the target object and keep that information with the target object.
 * It is intended to be assigned to a data object.
 * <p>
 * It is assumed that we are creating this introduction to be marshaled
 * with the target in a JSON document. Therefore, there are some Jackson
 * aspects added that are required to get the proxy marshaled correctly.
 */
@JsonSerialize(as=MyAccessIntroduction.class) //use this definition when marshaling proxy
public interface MyAccessIntroduction<T> {
    enum Access { MEMBER, OWNER, ADMIN }
    List<Access> getUserRoles();
    void setUserRoles(List<Access> roles);

    //Don't try to marshall raw target. The proxy will replace returned raw
    // target with the overall proxy and cause cycle
    @JsonIgnore
    T getData();

    /**
     * Instead of returning a raw target, hide the target from proxy within a
     * wrapper but eliminate the wrapper when marshalling. An alternative would
     * have been to clone the data target. This should be much cheaper.
     */
    @JsonUnwrapped
    TargetWrapper<T> getRawTarget();

    record TargetWrapper<T>(@JsonUnwrapped T data){}
}
