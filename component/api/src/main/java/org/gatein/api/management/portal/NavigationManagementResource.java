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

import org.gatein.api.Portal;
import org.gatein.api.portal.Label;
import org.gatein.api.portal.Localized;
import org.gatein.api.portal.navigation.Navigation;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodePath;
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
import org.gatein.management.api.operation.OperationNames;

import static org.gatein.api.portal.navigation.Nodes.*;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@Managed
public class NavigationManagementResource
{
   private final Portal portal;
   private final SiteId siteId;

   public NavigationManagementResource(Portal portal, SiteId siteId)
   {
      this.portal = portal;
      this.siteId = siteId;
   }

   @Managed
   public ModelObject getNavigation(@ManagedContext ModelObject model, @ManagedContext PathAddress address)
   {
      // Populate the model
      populateModel(portal.getNavigation(siteId, visitChildren(), null), model, address);

      return model;
   }

   @Managed("{path: .*}")
   public ModelObject getNode(@MappedPath("path") String path, @ManagedContext ModelObject model)
   {
      Node node = portal.getNode(siteId, path(path));

      if (node == null) throw new ResourceNotFoundException("Node not found for path "  + path);

      // Populate the model
      populateModel(node, model);

      return model;
   }

   @Managed("{path: .*}")
   @ManagedOperation(name = OperationNames.REMOVE_RESOURCE, description = "Removes the navigation node")
   public void removeNode(@MappedPath("path") String path, @ManagedContext ModelObject model)
   {
      Node node = portal.getNode(siteId, path(path));
      if (node == null) throw new ResourceNotFoundException("Node not found for path "  + path);

      Node parent = node.getParent();
      parent.removeChild(node.getName());
      portal.saveNode(parent);
   }

   @Managed("{path: .*}")
   @ManagedOperation(name = OperationNames.ADD_RESOURCE, description = "Adds the navigation node")
   public ModelObject addNode(@MappedPath("path") String path,
                              @ManagedContext ModelObject model)
   {

      NodePath nodePath = path(path);
      Node parent = getNode(nodePath.getParent(), true);

      Node node = new Node(nodePath.getLastSegment());
      parent.addChild(node);

      portal.saveNode(node);

      populateModel(node, model);

      return model;
   }

   private void populateModel(Navigation navigation, ModelObject model, PathAddress address)
   {
      model.set("priority", navigation.getPriority());
      ModelList modelNodes = model.get("node").setEmptyList();
      for (Node node : navigation.getChildren())
      {
         ModelReference modelNode = modelNodes.add().asValue(ModelReference.class);
         modelNode.set("name", node.getName());

         modelNode.set(address.append(node.getName()));
      }
   }

   private void populateModel(Node node, ModelObject model)
   {
      model.set("name", node.getName());
      Label label = portal.resolveLabel(node.getLabel());
      ModelList modelLabel = model.get("label", ModelList.class);
      if (label.isLocalized())
      {
         for (Localized.Value<String> value : label.getLocalizedValues())
         {
            String localeString = value.getLocale().getLanguage();
            if (localeString == null)
            {
               throw new RuntimeException("Language was null for locale " + value.getLocale());
            }
            String country = value.getLocale().getCountry();
            if (country != null && country.length() > 0)
            {
               localeString += "-" + country.toLowerCase();
            }

            modelLabel.add().asValue(ModelObject.class)
               .set("value", value.getValue()).set("lang", localeString);
         }
      }
      else
      {
         modelLabel.add().asValue(ModelObject.class).set("value", label.getValue());
      }
   }

   private Node getNode(NodePath path, boolean require)
   {
      Node node = portal.getNode(siteId, path);
      if (node == null && require) throw new ResourceNotFoundException("Node not found for path " + path);

      return node;
   }
}
