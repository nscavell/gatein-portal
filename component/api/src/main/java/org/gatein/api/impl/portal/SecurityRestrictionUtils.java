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

package org.gatein.api.impl.portal;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
class SecurityRestrictionUtils
{
   /*private SecurityRestrictionUtils(){}

   public static SecurityRestriction forPermission(SecurityRestriction.Type type, String permission) throws IllegalArgumentException
   {
      if (permission == null) return null;

      switch (type)
      {
         case ACCESS:
            return SecurityRestriction.access().addEntry(createEntry(permission));
         case EDIT:
            return SecurityRestriction.edit(createEntry(permission));
         default:
            throw new IllegalArgumentException("Unsupported SecurityRestriction type " + type);
      }
   }

   public static SecurityRestriction forPermissions(SecurityRestriction.Type type, Iterable<String> permissions) throws IllegalArgumentException
   {
      if (permissions == null) return null;

      SecurityRestriction restriction = null;
      for (String permission : permissions)
      {
         if (permission == null) continue;
         switch (type)
         {
            case ACCESS:
               if (restriction == null) restriction = SecurityRestriction.access();

               restriction.addEntry(createEntry(permission));
               break;
            case EDIT:
               if (restriction == null)
               {
                  restriction = SecurityRestriction.edit(createEntry(permission));
               }
               else
               {
                  restriction.addEntry(createEntry(permission));
               }
               break;
            default:
               throw new IllegalArgumentException("Unsupported SecurityRestriction type " + type);
         }
      }

      return restriction;
   }

   public static SecurityRestriction.Entry createEntry(String permission) throws IllegalArgumentException
   {
      if (permission == null) return null;

      if (permission.equals("Everyone")) return SecurityRestriction.Entry.publicEntry();

      String[] parts = permission.split(":");
      if (parts.length != 2) throw new IllegalArgumentException("Invalid permission " + permission);

      return SecurityRestriction.Entry.create(parts[0], parts[1]);
   }*/
}
