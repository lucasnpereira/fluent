package org.fluentj.core.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Created by lucas on 13/02/17.
 */
@Target(ElementType.TYPE)
public @interface GenerateExtensibleFluentClass {
    String className() default "";
}
