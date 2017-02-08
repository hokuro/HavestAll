package basashi.havall.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ java.lang.annotation.ElementType.FIELD })
public @interface ConfigProperty {
	String category() default "general";
	String comment() default "";
	boolean isSave() default false;
}