/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.gatein.api.page;

import org.exoplatform.portal.mop.page.PageContext;
import org.exoplatform.portal.mop.page.PageState;
import org.gatein.api.Parameters;
import org.gatein.api.Util;
import org.gatein.api.security.Permission;
import org.gatein.api.site.SiteId;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class PageImpl implements Page {
    private final transient PageContext pageContext;

    public PageImpl(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    @Override
    public PageId getId() {
        return Util.from(pageContext.getKey());
    }

    @Override
    public SiteId getSiteId() {
        return Util.from(pageContext.getKey().getSite());
    }

    @Override
    public String getName() {
        return pageContext.getKey().getName();
    }

    @Override
    public String getDescription() {
        return pageContext.getState().getDescription();
    }

    @Override
    public void setDescription(String description) {
       setState(builder().description(description));
    }

    @Override
    public void setDisplayName(String displayName) {
       setState(builder().displayName(displayName));
    }

    @Override
    public String getDisplayName() {
        return pageContext.getState().getDisplayName();
    }

    @Override
    public Permission getAccessPermission() {
        return Util.from(pageContext.getState().getAccessPermissions());
    }

    @Override
    public void setAccessPermission(Permission permission) {
        Parameters.requireNonNull(permission, "permission", "To allow access to everyone use Permission.everyone()");

        setState(builder().accessPermissions(Util.from(permission)));
    }

    @Override
    public Permission getEditPermission() {
        return Util.from(pageContext.getState().getEditPermission());
    }

    @Override
    public void setEditPermission(Permission permission) {
        Parameters.requireNonNull(permission, "permission", "To allow edit for everyone use Permission.everyone()");

        // Only one edit permission (membership) is allowed at this time.
        String[] permissions = Util.from(permission);
        if (permissions.length != 1)
            throw new IllegalArgumentException("Invalid permission. Only one membership is allowed for an edit permission");

        setState(builder().editPermission(permissions[0]));
    }

    @Override
    public int compareTo(Page page) {
        return getName().compareTo(page.getName());
    }

    public PageContext getPageContext() {
        return pageContext;
    }

    private PageState.Builder builder() {
        return pageContext.getState().builder();
    }

    private void setState(PageState.Builder builder) {
        pageContext.setState(builder.build());
    }
}
