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

import static org.gatein.api.management.GateInApiManagementResource.getPagesAddress;

import java.util.Date;

import org.gatein.api.Portal;
import org.gatein.api.navigation.Navigation;
import org.gatein.api.navigation.Node;
import org.gatein.api.navigation.NodePath;
import org.gatein.api.navigation.NodeVisitor;
import org.gatein.api.navigation.Nodes;
import org.gatein.api.navigation.Visibility;
import org.gatein.api.page.PageId;
import org.gatein.api.site.SiteId;
import org.gatein.management.api.PathAddress;
import org.gatein.management.api.annotations.Managed;
import org.gatein.management.api.annotations.ManagedContext;
import org.gatein.management.api.annotations.ManagedOperation;
import org.gatein.management.api.annotations.MappedAttribute;
import org.gatein.management.api.annotations.MappedPath;
import org.gatein.management.api.exceptions.OperationException;
import org.gatein.management.api.exceptions.ResourceNotFoundException;
import org.gatein.management.api.model.Model;
import org.gatein.management.api.model.ModelList;
import org.gatein.management.api.model.ModelObject;
import org.gatein.management.api.model.ModelProvider;
import org.gatein.management.api.model.ModelReference;
import org.gatein.management.api.operation.OperationNames;

/**
 * @author <a href="mailto:nscavell@redhat.com">Nick Scavelli</a>
 */
@Managed
@SuppressWarnings("unused")
public class NavigationManagementResource {
    private final Navigation navigation;
    private final ModelProvider modelProvider;

    public NavigationManagementResource(Portal portal, ModelProvider modelProvider, SiteId siteId) {
        this.navigation = portal.getNavigation(siteId);
        this.modelProvider = modelProvider;
    }

    @Managed
    public ModelObject getNavigation(@ManagedContext PathAddress address, @MappedAttribute("scope") String scopeAttribute) {
        // Populate the model
        ModelObject model = modelProvider.newModel(ModelObject.class);

        NodeVisitor visitor = Nodes.visitChildren();
        int scope = 0;
        if (scopeAttribute != null) {
            scope = Integer.parseInt(scopeAttribute);
            visitor = Nodes.visitNodes(scope);
        }

        Node node = getNode(NodePath.root(), true, visitor);
        populateNavigationModel(node, scope, model, address);

        return model;
    }

    @Managed("{path: .*}")
    public ModelObject getNode(@MappedPath("path") String path, @MappedAttribute("scope") String scopeAttribute,
            @ManagedContext PathAddress address) {
        NodeVisitor visitor = Nodes.visitChildren();
        int scope = 0;
        if (scopeAttribute != null) {
            scope = Integer.parseInt(scopeAttribute);
            visitor = Nodes.visitNodes(scope);
        }
        Node node = getNode(path, true, visitor);

        // Populate the model
        ModelObject model = modelProvider.newModel(ModelObject.class);
        populateNode(node, scope, model, address);

        return model;
    }

    @Managed("{path: .*}")
    @ManagedOperation(name = OperationNames.REMOVE_RESOURCE, description = "Removes the navigation node")
    public void removeNode(@MappedPath("path") String path) {
        Node node = getNode(path, true);

        Node parent = node.getParent();
        parent.removeChild(node.getName());
        navigation.saveNode(parent);
    }

    @Managed("{path: .*}")
    @ManagedOperation(name = OperationNames.ADD_RESOURCE, description = "Adds the navigation node")
    public ModelObject addNode(@MappedPath("path") String path, @ManagedContext PathAddress address) {
        NodePath nodePath = NodePath.fromString(path);
        Node parent = getNode(nodePath.parent(), true, Nodes.visitChildren());
        String name = nodePath.getLastSegment();

        if (parent.hasChild(name)) {
            throw new OperationException(OperationNames.ADD_RESOURCE, "Node already exists for " + nodePath);
        }

        // Add child and save
        Node child = parent.addChild(name);
        navigation.saveNode(parent);

        // Populate model
        ModelObject model = modelProvider.newModel(ModelObject.class);
        populateNode(child, 0, model, address);

        return model;
    }

    @Managed("{path: .*}")
    @ManagedOperation(name = OperationNames.UPDATE_RESOURCE, description = "Updates the navigation node")
    public ModelObject updateNode(@MappedPath("path") String path, @ManagedContext ModelObject nodeModel) {
        // TODO: Implement

        return nodeModel;
    }

    private Node getNode(String pathString, boolean require) {
        return getNode(pathString, require, Nodes.visitNone());
    }

    private Node getNode(String pathString, boolean require, NodeVisitor visitor) {
        return getNode(NodePath.fromString(pathString), require, visitor);
    }

    private Node getNode(NodePath path, boolean require) {
        return getNode(path, require, Nodes.visitNone());
    }

    private Node getNode(NodePath path, boolean require, NodeVisitor visitor) {
        Node node = navigation.getNode(path, visitor);
        if (node == null && require)
            throw new ResourceNotFoundException("Node not found for path " + path);

        return node;
    }

    private void populateNavigationModel(Node rootNode, int scope, ModelObject model, PathAddress address) {
        model.set("priority", navigation.getPriority());
        model.set("siteType", navigation.getSiteId().getType().getName());
        model.set("siteName", navigation.getSiteId().getName());
        ModelList nodesModel = model.get("nodes").setEmptyList();
        if (rootNode.isChildrenLoaded()) {
            for (Node child : rootNode) {
                Model childModel = nodesModel.add();
                PathAddress childAddress = address.append(child.getName());
                if (scope > 0 || scope < 0) // Continue populating nodes in response
                {
                    populateNode(child, scope - 1, childModel.setEmptyObject(), childAddress);
                } else { // Populate node reference which can be followed
                    ModelReference nodeRef = childModel.set(childAddress);
                    nodeRef.set("name", child.getName());
                }
            }
        }
    }

    private void populateNode(Node node, int scope, ModelObject model, PathAddress address) {
        model.set("name", node.getName());
        ModelUtils.set("uri", node.getURI(), model);
        model.set("isVisible", node.isVisible());
        populateVisibility(node.getVisibility(), model.get("visibility", ModelObject.class));
        model.set("iconName", node.getIconName());

        // Display name
        model.set("displayName", node.getDisplayName());
        ModelUtils.populate("displayNames", node.getDisplayNames(), model);

        // Children nodes
        ModelList children = model.get("children", ModelList.class);
        if (node.isChildrenLoaded()) {
            for (Node child : node) {
                Model childModel = children.add();
                PathAddress childAddress = address.append(child.getName());
                if (scope > 0 || scope < 0) // Continue populating nodes in response
                {
                    populateNode(child, scope - 1, childModel.setEmptyObject(), childAddress);
                } else { // Populate node reference which can be followed
                    ModelReference nodeRef = childModel.set(childAddress);
                    nodeRef.set("name", child.getName());
                }
            }
        }
        // Page reference
        ModelReference pageRef = model.get("page").asValue(ModelReference.class);
        if (node.getPageId() != null) {
            PageId pageId = node.getPageId();
            PathAddress pageAddress = getPagesAddress(pageId.getSiteId()).append(pageId.getPageName());
            pageRef.set(pageAddress);
            pageRef.set("pageName", pageId.getPageName());
        }
    }

    private void populateVisibility(Visibility visibility, ModelObject model) {
        if (visibility != null) {
            ModelUtils.set("status", visibility.getStatus(), model);
            if (visibility.getPublicationDate() != null) {
                ModelObject pubDateModel = model.get("publication-date", ModelObject.class);
                Date start = visibility.getPublicationDate().getStart();
                Date end = visibility.getPublicationDate().getEnd();
                ModelUtils.set("start", start, pubDateModel);
                ModelUtils.set("end", end, pubDateModel);
            }
        }
    }
}
