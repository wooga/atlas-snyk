package com.wooga.spock.extensions.snyk.interceptor

import groovy.transform.InheritConstructors
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo

@InheritConstructors
class SnykSharedFieldInterceptor extends SnykFieldInterceptor {
    @Override
    void interceptSetupSpecMethod(IMethodInvocation invocation) throws Throwable {
        downloadSnyk(invocation)
        invocation.proceed()
    }

    @Override
    void interceptCleanupSpecMethod(IMethodInvocation invocation) throws Throwable {
        try {
            invocation.proceed()
        } finally {
            destroyKeychain(invocation)
        }
    }

    @Override
    void install(FieldInfo info) {
        this.info = info
        final spec = info.getParent().getTopSpec()
        spec.setupSpecInterceptors.add(this)
        spec.cleanupSpecInterceptors.add(this)
    }
}
