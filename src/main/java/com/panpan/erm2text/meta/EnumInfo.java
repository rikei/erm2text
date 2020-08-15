package com.panpan.erm2text.meta;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 
 * @author liuhs
 *
 */
@Target(ElementType.TYPE)
@Retention(RUNTIME)
public @interface EnumInfo
{
	 String[] value();
}
