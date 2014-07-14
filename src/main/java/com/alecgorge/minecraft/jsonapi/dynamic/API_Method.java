package com.alecgorge.minecraft.jsonapi.dynamic;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface API_Method {
	String namespace() default "";
	String name() default "";
	String description() default "";
	String returnDescription() default "";
	
	String[] argumentDescriptions() default {};
	
	boolean isProvidedByV2API() default true;
}
