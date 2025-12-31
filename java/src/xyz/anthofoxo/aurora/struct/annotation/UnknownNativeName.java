package xyz.anthofoxo.aurora.struct.annotation;

import static java.lang.annotation.RetentionPolicy.SOURCE;

import java.lang.annotation.Retention;

/**
 * Classes annotated with this annotation are disclosing that the class name may
 * not match the same name used with the Thumper game.
 */
@Retention(SOURCE)
public @interface UnknownNativeName {

}
