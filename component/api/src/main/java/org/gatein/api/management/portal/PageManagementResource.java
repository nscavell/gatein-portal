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

package org.gatein.api.management.portal;

import java.util.List;

import org.gatein.api.EntityNotFoundException;
import org.gatein.api.Portal;
import org.gatein.api.portal.Membership;
import org.gatein.api.portal.Permission;
import org.gatein.api.portal.page.Page;
import org.gatein.api.portal.page.PageId;
import org.gatein.api.portal.page.PageQuery;
import org.gatein.api.portal.site.SiteId;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.annotations.Managed;
import org.gatein.management.api.annotations.ManagedContext;
import org.gatein.management.api.annotations.ManagedOperation;
import org.gatein.management.api.annotations.MappedPath;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.model.ModelList;
import org.gatein.management.api.model.ModelObject;
import org.gatein.management.api.model.ModelReference;
import org.gatein.management.api.model.ModelString;
import org.gatein.management.api.operation.OperationNames;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@Managed
public class PageManagementResource
{
   private final Portal portal;
   private final SiteId siteId;

   public PageManagementResource(Portal portal, SiteId siteId)
   {
      this.portal = portal;
      this.siteId = siteId;
   }

   @Managed(description = "Retrieves all pages for given site")
   public ModelList getPages(@ManagedContext ModelList list, @ManagedContext PathAddress address)
   {
      // Populate model
      populateModel(portal.findPages(new PageQuery.Builder().build()), list, address);

      return list;
   }

   @Managed("{page-name}")
   public ModelObject getPage(@MappedPath("page-name") String name, @ManagedContext ModelObject model)
   {
      Page page = portal.getPage(new PageId(siteId, name));
      if (page == null) throw new ResourceNotFoundException("Page " + name + " does not exist for site id " + siteId);

      // Populate model
      populateModel(page, model);

      return model;
   }

   @Managed("{page-name}")
   @ManagedOperation(name = OperationNames.REMOVE_RESOURCE, description = "Removes the given page from the portal")
   public void removePage(@MappedPath("page-name") String name)
   {
      try
      {
         portal.removePage(new PageId(siteId, name));
      }
      catch (EntityNotFoundException e)
      {
         throw new ResourceNotFoundException("Could not remove page because page id " + new PageId(siteId, name) + " does not exist.");
      }
   }

   @Managed("{page-name}")
   @ManagedOperation(name = OperationNames.ADD_RESOURCE, description = "Adds the given page to the portal")
   public ModelObject addPage(@MappedPath("page-name") String name, @ManagedContext ModelObject model)
   {
      //Page page = site.createPage(name);
      Page page = new Page(new PageId(siteId, name));
      portal.savePage(page);

      populateModel(page, model);

      return model;
   }

   private void populateModel(List<Page> pages, ModelList list, PathAddress address)
   {
      for (Page page : pages)
      {
         ModelReference pageRef = list.add().asValue(ModelReference.class);
         pageRef.set("name", page.getName());
         pageRef.set("siteType", page.getId().getSiteId().getType().name().toLowerCase());
         pageRef.set("siteName", page.getId().getSiteId().getName());
         pageRef.set(address.append(page.getName()));
      }
   }

   private void populateModel(Page page, ModelObject model)
   {
      model.set("name", page.getName());
      model.set("title", page.getTitle());
      populateModel(page.getEditPermission(), "edit-permissions", model);
      populateModel(page.getAccessPermission(), "access-permissions", model);
   }

   private void populateModel(Permission permission, String fieldName, ModelObject model)
   {
      if (permission != null)
      {
         ModelList list = model.get(fieldName).asValue(ModelList.class);
         if (permission.getMemberships().isEmpty())
         {
            list.add().asValue(ModelString.class).set("Everyone");
         }

         for (Membership membership : permission.getMemberships())
         {
            list.add().asValue(ModelString.class).set(membership.toString());
         }
      }
   }
}
