package info.ejava.examples.svc.aop.items.introductions;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.DeclareParents;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * This example AOP component defines an Introduction to be added to
 * components matching an expression and implements advice that will
 * track calls made to methods matching a pointcut.
 */
@Component
@Aspect
public class MyUsageAspect {
    /**
     * This will define an Introduction interface and default implementation
     * that will be added to subjects matching the provided expression.
     */
    @DeclareParents(value="info.ejava.examples.svc.aop.items.services.*", //matching expression
            defaultImpl = MyUsageIntroductionImpl.class)
    public static MyUsageIntroduction mixin; //signature

    /**
     * A pointcut to match any createItem method in the target package, taking and returning any argument.
     */
    @Pointcut("execution(* info.ejava.examples.svc.aop.items.services.*.createItem(..))") //expression
    public void anyCreateItem() {} /*signature*/

    /**
     * Advice that will run before each target implementing the MyUsageIntroduction
     * interface and is the subject of a createItem method call.
     * @param jp the object containing details of method call requested
     * @param usageIntro Introduction interface implemented by the proxy (this)
     *                   answering this intercept
     */
    @Before("anyCreateItem() && this(usageIntro)")
    public void recordCalled(JoinPoint jp, MyUsageIntroduction usageIntro) {
        Object arg = jp.getArgs()[0];
        usageIntro.addCalled(jp.getSignature().toString() + ":" + arg);
    }
}
