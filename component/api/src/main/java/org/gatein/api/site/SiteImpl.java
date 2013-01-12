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

package org.gatein.api.site;

import java.util.Locale;

import org.gatein.api.common.Attributes;
import org.gatein.api.internal.ObjectToStringBuilder;
import org.gatein.api.internal.Parameters;
import org.gatein.api.security.Group;
import org.gatein.api.security.Permission;
import org.gatein.api.security.User;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public class SiteImpl implements Site {
    private final SiteId id;

    private String displayName;
    private String description;
    private Locale locale;
    private String skin;
    private Attributes attributes;
    private Permission accessPermission;
    private Permission editPermission;

    private boolean create;

    public SiteImpl(String name) {
        this(new SiteId(name));
    }

    public SiteImpl(Group group) {
        this(new SiteId(group));
    }

    public SiteImpl(User user) {
        this(new SiteId(user));
    }

    public SiteImpl(SiteId id) {
        this.id = Parameters.requireNonNull(id, "id");
        this.attributes = new Attributes();
    }

    @Override
    public SiteId getId() {
        return id;
    }

    @Override
    public SiteType getType() {
        return id.getType();
    }

    @Override
    public String getName() {
        return id.getName();
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }


    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String getDisplayName() {
        // TODO: For sites of type SiteType.SPACE this should return the label of the group
        if (displayName == null) {
            return getName();
        }
        return displayName;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public String getSkin() {
        return skin;
    }

    @Override
    public void setSkin(String skin) {
        this.skin = skin;
    }

    @Override
    public Attributes getAttributes() {
        return attributes;
    }

    @Override
    public Permission getAccessPermission() {
        return accessPermission;
    }

    @Override
    public void setAccessPermission(Permission permission) {
        this.accessPermission = permission;
    }

    @Override
    public Permission getEditPermission() {
        return editPermission;
    }

    @Override
    public void setEditPermission(Permission permission) {
        this.editPermission = permission;
    }

    public boolean isCreate() {
        return create;
    }

    public void setCreate(boolean create) {
        this.create = create;
    }

    @Override
    public int compareTo(Site other) {
        return getName().compareTo(other.getName());
    }

    @Override
    public String toString() {
        return ObjectToStringBuilder.toStringBuilder(getClass()).add("type", getType().getName()).add("name", getName())
                .add("displayName", displayName).add("description", getDescription()).add("locale", getLocale())
                .add("skin", getSkin()).add("attributes", getAttributes()).add("editPermission", getEditPermission())
                .add("accessPermission", getAccessPermission()).toString();
    }

    public static final class AttributeKeys {
        public static final Attributes.Key<String> SESSION_BEHAVIOR = Attributes.key("org.gatein.api.portal.session_behavior",
                String.class);
        public static final Attributes.Key<Boolean> SHOW_PORTLET_INFO_BAR = Attributes.key(
                "org.gatein.api.portal.show_info_bar", Boolean.class);

        private AttributeKeys() {
        }
    }
}
