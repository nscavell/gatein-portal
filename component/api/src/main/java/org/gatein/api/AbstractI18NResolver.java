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
package org.gatein.api;

import java.util.Locale;
import java.util.ResourceBundle;

import org.exoplatform.commons.utils.ExpressionUtil;
import org.exoplatform.portal.mop.Described;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.gatein.api.common.i18n.LocalizedString;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
public abstract class AbstractI18NResolver {
    public abstract ResourceBundle getResourceBundle();

    public abstract Locale getUserLocale();

    public abstract Locale getSiteLocale();

    private final DescriptionService service;

    public AbstractI18NResolver(DescriptionService service) {
        this.service = service;
    }

    public String resolveName(String string, String defaultValue) {
        return resolveName(new LocalizedString(string), null, defaultValue);
    }

    public String resolveName(LocalizedString string, String descriptionId, String defaultValue) {
        return resolve(string, descriptionId, defaultValue, true);
    }

    public String resolveDescription(String string, String defaultValue) {
        return resolveDescription(new LocalizedString(string), null, defaultValue);
    }

    public String resolveDescription(LocalizedString string, String descriptionId, String defaultValue) {
        return resolve(string, descriptionId, defaultValue, false);
    }

    private String resolve(LocalizedString string, String descriptionId, String defaultValue, boolean nameFlag) {
        String resolved = null;

        if (string != null && !string.isLocalized()) {
            resolved = ExpressionUtil.getExpressionValue(getResourceBundle(), string.getValue());
        } else if (descriptionId != null) {
            Locale userLocale = getUserLocale();
            Locale siteLocale = getSiteLocale();
            Described.State described = service.resolveDescription(descriptionId, siteLocale, userLocale);
            if (described != null) {
                resolved = (nameFlag) ? described.getName() : described.getDescription();
            }
        }

        return (resolved == null) ? defaultValue : resolved;
    }
}
