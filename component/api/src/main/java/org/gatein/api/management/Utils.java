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

package org.gatein.api.management;

import org.gatein.api.Portal;
import org.gatein.api.PortalRequest;
import org.gatein.api.common.i18n.Localized;
import org.gatein.api.common.i18n.LocalizedString;
import org.gatein.api.internal.StringJoiner;
import org.gatein.api.page.Page;
import org.gatein.api.security.Membership;
import org.gatein.api.security.Permission;
import org.gatein.api.security.User;
import org.gatein.api.site.Site;
import org.gatein.management.api.exceptions.InvalidDataException;
import org.gatein.management.api.exceptions.NotAuthorizedException;
import org.gatein.management.api.model.Model;
import org.gatein.management.api.model.ModelBoolean;
import org.gatein.management.api.model.ModelList;
import org.gatein.management.api.model.ModelNumber;
import org.gatein.management.api.model.ModelObject;
import org.gatein.management.api.model.ModelReference;
import org.gatein.management.api.model.ModelString;
import org.gatein.management.api.model.ModelValue;
import org.gatein.management.api.operation.OperationContext;

import javax.xml.bind.DatatypeConverter;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class Utils {
    private Utils() {
    }

    public static void verifyAccess(Site site, OperationContext context) throws NotAuthorizedException {
        if (!hasPermission(site.getAccessPermission())) {
            throw new NotAuthorizedException(context.getUser(), context.getOperationName());
        }
    }

    public static void verifyAccess(Page page, OperationContext context) throws NotAuthorizedException {
        if (!hasPermission(page.getAccessPermission())) {
            throw new NotAuthorizedException(context.getUser(), context.getOperationName());
        }
    }

    public static boolean hasPermission(Permission permission) {
        PortalRequest request = PortalRequest.getInstance();
        Portal portal = request.getPortal();
        User user = request.getUser();

        return portal.hasPermission(user, permission);
    }

    public static void populate(String fieldName, LocalizedString string, ModelObject model) {
        if (string == null)
            return;

        ModelList list = model.get(fieldName, ModelList.class);
        if (string.isLocalized()) {
            for (Localized.Value<String> value : string.getLocalizedValues()) {
                String localeString = value.getLocale().getLanguage();
                if (localeString == null) {
                    throw new RuntimeException("Language was null for locale " + value.getLocale());
                }
                String country = value.getLocale().getCountry();
                if (country != null && country.length() > 0) {
                    localeString += "-" + country.toLowerCase();
                }

                list.add().asValue(ModelObject.class).set("value", value.getValue()).set("lang", localeString);
            }
        } else {
            list.add().asValue(ModelObject.class).set("value", string.getValue());
        }
    }

    public static void populate(String fieldName, Permission permission, ModelObject model) {
        if (permission != null) {
            ModelList list = model.get(fieldName, ModelList.class);
            if (permission.isAccessibleToEveryone()) {
                list.add("Everyone");
            }

            for (Membership membership : permission.getMemberships()) {
                list.add(membership.toString());
            }
        }
    }

    public static void set(String name, Object value, ModelObject model) {
        String s = (value == null) ? null : value.toString();
        model.set(name, s);
    }

    public static void set(String name, Date value, ModelObject model) {
        String s = null;
        if (value != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(value);
            s = DatatypeConverter.printDateTime(cal);
        }

        set(name, s, model);
    }

    public static <T extends ModelValue> T get(ModelObject modelObject, Class<T> type, String...names) {
        Model model = modelObject.get(names);
        try {
            return model.asValue(type);
        } catch (IllegalArgumentException e) {
            ModelValue.ModelValueType expected;
            if (type == ModelString.class) {
                expected = ModelValue.ModelValueType.STRING;
            } else if (type == ModelNumber.class) {
                expected = ModelValue.ModelValueType.NUMBER;
            } else if (type == ModelBoolean.class) {
                expected = ModelValue.ModelValueType.BOOLEAN;
            } else if (type == ModelList.class) {
                expected = ModelValue.ModelValueType.LIST;
            } else if (type == ModelObject.class) {
                expected = ModelValue.ModelValueType.OBJECT;
            } else if (type == ModelReference.class) {
                expected = ModelValue.ModelValueType.REFERENCE;
            } else {
                expected = ModelValue.ModelValueType.UNDEFINED;
            }
            throw invalidType(model, expected, names);
        }
    }

    public static String nonNullString(ModelObject model, String... names) {
        ModelString string = get(model, ModelString.class, names);
        if (!string.isDefined()) {
            throw invalidValue(null, names);
        }
        String value = string.getValue();
        if (value == null) {
            throw invalidValue(null, names);
        }

        return value;
    }

    public static Date getDate(ModelObject model, String...names) {
        String string = get(model, ModelString.class, names).getValue();
        if (string != null) {
            try {
                return DatatypeConverter.parseDateTime(string).getTime();
            } catch (IllegalArgumentException e) {
                throw invalidValue(string, names);
            }
        }

        return null;
    }

    public static InvalidDataException invalidValue(String value, String...field) {
        return invalidData("Invalid value '" + value + "' for %s", field);
    }

    public static InvalidDataException invalidType(ModelValue value, ModelValue.ModelValueType type, String...field) {
        return invalidData("Invalid value type " + value.getValueType() + " for %s. Was expecting " + type, field);
    }

    public static InvalidDataException requiredField(String...field) {
        return invalidData("%s is required", field);
    }

    public static InvalidDataException requiredFieldWhen(String when, String...field) {
        return invalidData("%s is required when " + when, field);
    }

    public static InvalidDataException invalidData(String format, String... field) {
        if (field == null) {
            throw new InvalidDataException(format);
        }
        return new InvalidDataException(String.format(format, StringJoiner.joiner(".").join(field)));
    }
}
