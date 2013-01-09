package org.gatein.management.runtime;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.gatein.management.api.RuntimeContext;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 * @version $Revision$
 */
public class RuntimeContextImpl implements RuntimeContext {
    @Override
    public <T> T getRuntimeComponent(Class<T> componentClass) {
        return componentClass.cast(PortalContainer.getInstance().getComponentInstanceOfType(componentClass));
    }

    @Override
    public boolean isUserInRole(String role) {
        ConversationState state = ConversationState.getCurrent();
        if (state == null) {
            return false;
        }

        Identity identity = state.getIdentity();
        if (identity == null) {
            return false;
        }

        for (String availableRole : identity.getRoles()) {
            if (availableRole.equals(role)) return true;
        }

        return false;
    }
}
