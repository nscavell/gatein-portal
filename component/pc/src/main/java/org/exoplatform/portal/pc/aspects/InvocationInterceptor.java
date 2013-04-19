package org.exoplatform.portal.pc.aspects;

import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.portlet.PortletInvokerInterceptor;

/**
 * @author <a href="http://community.jboss.org/people/kenfinni">Ken Finnigan</a>
 */
public class InvocationInterceptor extends PortletInvokerInterceptor {

    private static final ThreadLocal<PortletInvocation> portletInvocation = new ThreadLocal<PortletInvocation>();

    public InvocationInterceptor() {
    }

    @Override
    public PortletInvocationResponse invoke(PortletInvocation invocation) throws IllegalArgumentException, PortletInvokerException {
        portletInvocation.set(invocation);
        try {
            return super.invoke(invocation);
        } finally {
            portletInvocation.remove();
        }
    }

    public static PortletInvocation getPortletInvocation() {
        return portletInvocation.get();
    }
}
