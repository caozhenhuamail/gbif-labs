/**
 * 
 */
package org.gbif.registry.ws.guice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Indicates that the field is suitable for String trimming.
 * To use this annotate the method with Validate, and the fields intended for trimming.
 * The field must be:
 * <ul>
 * <li>A mutable POJO with getters and setters</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER, ElementType.METHOD})
public @interface Trim {
}
