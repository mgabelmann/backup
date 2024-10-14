package mgabelmann;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * To be ignored by coverage plugin Jacoco you must include this annotation or any annotation with
 * 'Generated' in the name. We create a custom one so people will understand why since we
 * sometimes want to ignore classes that are not generated.
 */
@Documented
@Retention(RUNTIME)
@Target({TYPE, METHOD, CONSTRUCTOR})
public @interface IgnoreJacocoGenerated {

}
