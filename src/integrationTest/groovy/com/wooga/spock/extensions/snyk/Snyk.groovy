package com.wooga.spock.extensions.snyk


import org.spockframework.runtime.extension.ExtensionAnnotation

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.FIELD, ElementType.METHOD])
@ExtensionAnnotation(SnykExtension.class)

@interface Snyk {
    String executableName() default "snyk"
    String version() default "latest"
}

