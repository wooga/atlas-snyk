package com.wooga.spock.extensions.snyk.interceptor

import groovy.transform.InheritConstructors
import org.spockframework.runtime.extension.IMethodInvocation
import org.spockframework.runtime.model.FieldInfo
import spock.lang.Specification

@InheritConstructors
class SnykFieldInterceptor extends SnykInterceptor<FieldInfo> {

    @Override
    void interceptCleanupMethod(IMethodInvocation invocation) throws Throwable {
        try {
            invocation.proceed()
        } finally {
            destroyKeychain(invocation)
        }
    }

    void install(FieldInfo info) {
        this.info = info
        final spec = info.parent.getTopSpec()
        spec.setupInterceptors.add(this)
        spec.cleanupInterceptors.add(this)
    }

    @Override
    File downloadSnyk(IMethodInvocation invocation) {
        def snyk = super.downloadSnyk(invocation)
        def spec = getSpec(invocation)
        info.writeValue(spec, snyk)
        snyk
    }

    @Override
    void interceptSetupMethod(IMethodInvocation invocation) throws Throwable {
        downloadSnyk(invocation)
        invocation.proceed()
    }

    void destroyKeychain(IMethodInvocation methodInvocation) {
        File snyk = getKeychain(methodInvocation)
        snyk.delete()
    }

    protected Specification getSpec(IMethodInvocation invocation) {
        ((info.shared) ? invocation.sharedInstance : invocation.instance) as Specification
    }

    protected File getKeychain(IMethodInvocation invocation) {
        final specInstance = getSpec(invocation)
        info.readValue(specInstance) as File
    }
}

