package com.wooga.spock.extensions.snyk

import com.wooga.spock.extensions.snyk.interceptor.SnykFieldInterceptor
import com.wooga.spock.extensions.snyk.interceptor.SnykInterceptor
import com.wooga.spock.extensions.snyk.interceptor.SnykSharedFieldInterceptor
import org.spockframework.runtime.extension.AbstractAnnotationDrivenExtension
import org.spockframework.runtime.model.FieldInfo

class SnykExtension extends AbstractAnnotationDrivenExtension<Snyk> {
    @Override
    void visitFieldAnnotation(Snyk annotation, FieldInfo field) {
        SnykInterceptor interceptor

        if (field.isShared()) {
            interceptor = new SnykSharedFieldInterceptor(annotation)
        } else {
            interceptor = new SnykFieldInterceptor(annotation)
        }

        interceptor.install(field)
    }
}
