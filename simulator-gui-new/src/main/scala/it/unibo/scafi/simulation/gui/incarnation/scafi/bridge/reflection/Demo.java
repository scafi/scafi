package it.unibo.scafi.simulation.gui.incarnation.scafi.bridge.reflection;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * mark a class as a scafi demo
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Demo {
    /**
     * @return a demo description
     */
    String description() default "";

    /**
     * @return the simulation type
     */
    SimulationType simulationType() default SimulationType.STANDARD;
}

