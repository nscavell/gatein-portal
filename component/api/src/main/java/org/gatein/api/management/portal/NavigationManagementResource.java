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

import static org.gatein.api.navigation.Nodes.visitChildren;
import static org.gatein.api.navigation.Nodes.visitNone;

import org.gatein.api.Portal;
import org.gatein.api.navigation.Navigation;
import org.gatein.api.navigation.Node;
import org.gatein.api.navigation.NodePath;
import org.gatein.api.navigation.Nodes;
import org.gatein.api.site.SiteId;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.annotations.Managed;
import org.gatein.management.api.annotations.ManagedContext;
import org.gatein.management.api.annotations.ManagedOperation;
import org.gatein.management.api.annotations.MappedPath;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.model.ModelList;
import org.gatein.management.api.model.ModelObject;
import org.gatein.management.api.model.ModelProvider;
import org.gatein.management.api.model.ModelReference;
import org.gatein.management.api.operation.OperationNames;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@Managed
public class NavigationManagementResource
{
   private final Navigation navigation;
   private final ModelProvider modelProvider;

   public NavigationManagementResource(Portal portal, ModelProvider modelProvider, SiteId siteId)
   {
      this.navigation = portal.getNavigation(siteId);
      this.modelProvider = modelProvider;
   }

   @Managed
   public ModelObject getNavigation(@ManagedContext PathAddress address)
   {
      // Populate the model
      ModelObject model = modelProvider.newModel(ModelObject.class);
      populateModel(model, address);

      return model;
   }

   @Managed("{path: .*}")
   public ModelObject getNode(@MappedPath("path") String path)
   {
      Node node = navigation.getNode(NodePath.fromString(path), visitNone());

      if (node == null) throw new ResourceNotFoundException("Node not found for path "  + path);

      // Populate the model
      ModelObject model = modelProvider.newModel(ModelObject.class);
      populateModel(node, model);

      return model;
   }

   @Managed("{path: .*}")
   @ManagedOperation(name = OperationNames.REMOVE_RESOURCE, description = "Removes the navigation node")
   public void removeNode(@MappedPath("path") String path)
   {
      Node node = navigation.getNode(NodePath.fromString(path), visitNone());
      if (node == null) throw new ResourceNotFoundException("Node not found for path "  + path);

      Node parent = node.getParent();
      parent.removeChild(node.getName());
      navigation.saveNode(parent);
   }

   @Managed("{path: .*}")
   @ManagedOperation(name = OperationNames.ADD_RESOURCE, description = "Adds the navigation node")
   public ModelObject addNode(@MappedPath("path") String path)
   {
      NodePath nodePath = NodePath.fromString(path);
      Node parent = navigation.getNode(NodePath.fromString(path), Nodes.visitNone());

      Node node = parent.addChild(nodePath.getLastSegment());

      navigation.saveNode(node);

      ModelObject model = modelProvider.newModel(ModelObject.class);
      populateModel(node, model);

      return model;
   }

   private void populateModel(ModelObject model, PathAddress address)
   {
      Node node = navigation.getRootNode(visitChildren());
      model.set("priority", navigation.getPriority());
      ModelList modelNodes = model.get("node").setEmptyList();
      for (Node n : node)
      {
         ModelReference modelNode = modelNodes.add().asValue(ModelReference.class);
         modelNode.set("name", n.getName());

         modelNode.set(address.append(n.getName()));
      }
   }

   private void populateModel(Node node, ModelObject model)
   {
      model.set("name", node.getName());
      ModelUtils.populate(node.getDisplayNames(), "displayName", model);
   }

   private Node getNode(NodePath path, boolean require)
   {
      Node node = navigation.getNode(path);
      if (node == null && require) throw new ResourceNotFoundException("Node not found for path " + path);

      return node;
   }
}
