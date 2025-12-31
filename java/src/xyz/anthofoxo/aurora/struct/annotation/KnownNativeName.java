package xyz.anthofoxo.aurora.struct.annotation;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;

/**
 * Classes annotated with this annotation declare that the class name is
 * verified to match the same structure name used in Thumper.
 */
@Retention(SOURCE)
public @interface KnownNativeName {

}
