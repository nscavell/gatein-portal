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

package org.gatein.api.navigation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.gatein.api.AbstractApiTest;
import org.gatein.api.EntityAlreadyExistsException;
import org.gatein.api.EntityNotFoundException;
import org.gatein.api.common.i18n.LocalizedString;
import org.gatein.api.site.SiteId;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public class NavigationImplTest extends AbstractApiTest {

    private Navigation navigation;

    @Test
    public void addChild() {
        createNavigationChildren();

        Node root = navigation.getRootNode(Nodes.visitAll());
        Node node = root.addChild("parent2");

        assertNotNull(root.getChild("parent2"));

        navigation.saveNode(node);

        root = navigation.getRootNode(Nodes.visitAll());
        assertNotNull(root.getChild("parent2"));
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void addChildExisting() {
        createNavigationChildren();

        Node node = navigation.getRootNode(Nodes.visitAll());
        node.getChild("parent").addChild("child");
    }

    @Test(expected = IllegalStateException.class)
    public void addChildNotExpanded() {
        createNavigationChildren();

        Node node = navigation.getRootNode(Nodes.visitChildren());
        node.getChild("parent").addChild("child");
    }

    @Before
    public void before() throws Exception {
        super.before();

        navigation = portal.getNavigation(siteId);
    }

    @Test
    public void createNavigationChildren() {
        Node node = navigation.getRootNode(Nodes.visitAll());

        Node parent = node.addChild("parent");
        parent.addChild("child");

        navigation.saveNode(node);

        navigation = portal.getNavigation(siteId);
        node = navigation.getRootNode(Nodes.visitAll());

        assertEquals(1, node.getChildCount());
        assertEquals(1, node.getChild("parent").getChildCount());
        assertEquals(0, node.getChild("parent").getChild("child").getChildCount());
    }

    @Test
    public void createNavigationEmpty() {
        navigation.setPriority(10);

        navigation = portal.getNavigation(siteId);

        assertEquals(10, navigation.getPriority());
        assertEquals(siteId, navigation.getSiteId());
        assertEquals(0, navigation.getRootNode(Nodes.visitAll()).getChildCount());
    }

    @Test
    public void removeNode() {
        createNavigationChildren();

        assertTrue(navigation.removeNode(NodePath.path("parent", "child")));

        Node node = navigation.getRootNode(Nodes.visitAll());
        assertEquals(0, node.getChild("parent").getChildCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void removeNode_NullNode() {
        navigation.removeNode(null);
    }

    @Test(expected = EntityNotFoundException.class)
    public void removeNode_NodeNotFound() {
        navigation.removeNode(NodePath.path("nosuch"));
    }

    @Test
    public void getChild() {
        createNavigationChildren();

        Node node = navigation.getRootNode(Nodes.visitAll());
        assertNotNull(node);
        assertTrue(node.isChildrenLoaded());
        assertTrue(node.getChild("parent").getChild("child").isChildrenLoaded());

        node = navigation.getRootNode(Nodes.visitChildren());
        assertNotNull(node);
        assertTrue(node.isChildrenLoaded());
        assertFalse(node.getChild("parent").isChildrenLoaded());
    }

    @Test
    public void getNode() {
        createNavigationChildren();

        Node node = navigation.getNode("parent", "child");
        assertNotNull(node);
        assertEquals("child", node.getName());
    }

    @Test
    public void getNode_With_Visitor() {
        createNavigationChildren();

        Node node = navigation.getNode(NodePath.path("parent"), Nodes.visitNone());
        assertNotNull(node);
        assertFalse(node.isChildrenLoaded());

        node = navigation.getNode(NodePath.path("parent"), Nodes.visitChildren());
        assertTrue(node.isChildrenLoaded());
        assertNotNull(node.getChild("child"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNode_EmptyPath() {
        String[] path = new String[0];
        navigation.getNode(path);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNode_NullNodePath() {
        navigation.getNode((NodePath) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNode_NullVisitor() {
        navigation.getNode(NodePath.path("parent"), null);
    }

    @Test
    public void getNode_Invalid_Path() {
        createNavigationChildren();

        Node node = navigation.getNode("foo", "child");
        assertNull(node);
    }

    @Test(expected = EntityNotFoundException.class)
    public void getNavigation_InvalidSite() {
        portal.getNavigation(new SiteId("invalid")).getRootNode(Nodes.visitAll());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNavigation_NullSiteId() {
        portal.getNavigation(null);
    }

    @Test
    public void displayName_extended() {
        Node node = navigation.getRootNode(Nodes.visitAll());

        Node n = node.addChild("parent");

        Map<Locale, String> m = new HashMap<Locale, String>();
        m.put(Locale.ENGLISH, "extended");
        m.put(Locale.FRENCH, "prolongé");

        n.setDisplayNames(new LocalizedString(m));

        navigation.saveNode(node);

        n = navigation.getRootNode(Nodes.visitChildren()).getChild("parent");

        assertNotNull(n.getDisplayNames());
        assertTrue(n.getDisplayNames().isLocalized());
        assertEquals("extended", n.getDisplayNames().getValue(Locale.ENGLISH));
        assertEquals("prolongé", n.getDisplayNames().getValue(Locale.FRENCH));
    }

    @Test
    public void displayName_simple() {
        Node node = navigation.getRootNode(Nodes.visitAll());

        Node n = node.addChild("parent");
        n.setDisplayName("simple");

        navigation.saveNode(n);

        assertEquals("simple", n.getDisplayNames().getValue());

        n = navigation.getRootNode(Nodes.visitChildren()).getChild("parent");

        assertNotNull(n.getDisplayNames());
        assertEquals("simple", n.getDisplayNames().getValue());
        assertFalse(n.getDisplayNames().isLocalized());
    }

    @Test(expected = IllegalArgumentException.class)
    public void loadNodes_NullVisitor() {
        navigation.getRootNode(null);
    }

    @Test
    public void moveNode() {
        Node root = navigation.getRootNode(Nodes.visitChildren());
        root.addChild("a").addChild("b").addChild("c").addChild("d");
        root.addChild("e").addChild("f").addChild("g").addChild("h");

        navigation.saveNode(root);

        root = navigation.getRootNode(Nodes.visitAll());

        Node d = root.getNode("a", "b", "c", "d");
        Node h = root.getNode("e", "f", "g", "h");
        navigation.refreshNode(h, Nodes.visitChildren());
        d.moveTo(h);

        navigation.saveNode(root);

        root = navigation.getRootNode(Nodes.visitAll());
        assertNull(root.getNode("a", "b", "c", "d"));
        assertNotNull(root.getNode("e", "f", "g", "h", "d"));
    }

    @Test
    public void refreshNode() {
        createNavigationChildren();

        Node nodeA = navigation.getRootNode(Nodes.visitAll());
        Node nodeB = navigation.getRootNode(Nodes.visitAll());

        nodeA.addChild("childA");
        navigation.saveNode(nodeA);

        assertNull(nodeB.getChild("childA"));
        navigation.refreshNode(nodeB);
        assertNotNull(nodeB.getChild("childA"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void refreshNode_NullNode() {
        navigation.refreshNode(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void refreshNode_NullVisitor() {
        navigation.refreshNode(navigation.getNode(NodePath.path("parent"), null));
    }

    @Test
    public void refreshNode_WithVisitor() {
        createNavigationChildren();

        Node nodeA = navigation.getRootNode(Nodes.visitAll());
        Node nodeB = navigation.getRootNode(Nodes.visitAll());

        nodeA.addChild("childA").addChild("childA-1").addChild("childA-1-1");
        navigation.saveNode(nodeA);

        assertNull(nodeB.getChild("childA"));
        navigation.refreshNode(nodeB, Nodes.visitNodes(3));
        assertNotNull(nodeB.getChild("childA"));
        assertNotNull(nodeB.getChild("childA").getChild("childA-1"));
        assertNotNull(nodeB.getChild("childA").getChild("childA-1").getChild("childA-1-1"));
    }

    @Test
    public void refreshNode_LoadChildren() {
        createNavigationChildren();

        Node n = navigation.getRootNode(Nodes.visitChildren());
        Node p = n.getChild("parent");

        assertTrue(n.isChildrenLoaded());
        assertFalse(p.isChildrenLoaded());

        navigation.refreshNode(p, Nodes.visitChildren());

        assertTrue(p.isChildrenLoaded());
    }

    @Test
    public void saveNode() throws InterruptedException {
        createNavigationChildren();

        Node node = navigation.getRootNode(Nodes.visitAll());

        Node parent = node.getChild("parent");

        parent.addChild("child2");

        assertNull(navigation.getRootNode(Nodes.visitAll()).getChild("parent").getChild("child2"));

        navigation.saveNode(parent);

        assertNotNull(navigation.getRootNode(Nodes.visitAll()).getChild("parent").getChild("child2"));
    }

    @Test
    public void saveNode_SaveChildSavesParent() {
        createNavigationChildren();

        Node node = navigation.getRootNode(Nodes.visitAll());

        Node parent = node.getChild("parent");
        parent.setIconName("new");

        Node child = parent.getNode("child");
        child.setIconName("new");

        parent.addChild("child2");

        navigation.saveNode(child);

        node = navigation.getRootNode(Nodes.visitAll());

        assertEquals("new", node.getChild("parent").getIconName());
        assertEquals(2, node.getChild("parent").getChildCount());
        assertEquals("new", node.getChild("parent").getChild("child").getIconName());
    }

    @Test
    public void saveNode_Merge() {
        createNavigationChildren();

        Node nodeA = navigation.getRootNode(Nodes.visitAll());
        Node nodeB = navigation.getRootNode(Nodes.visitAll());

        nodeA.addChild("childA");
        navigation.saveNode(nodeA);

        nodeB.addChild("childB");
        navigation.saveNode(nodeB);

        Node nodeC = navigation.getRootNode(Nodes.visitAll());

        assertNotNull(nodeC.getChild("childA"));
        assertNotNull(nodeC.getChild("childB"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void saveNode_NullNode() {
        navigation.saveNode(null);
    }

    @Test
    public void serialization() throws Exception {
        createNavigationChildren();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        Node parent = navigation.getNode(NodePath.path("parent"), Nodes.visitChildren());
        Node child = parent.getChild("child");
        navigation.refreshNode(child, Nodes.visitChildren());

        // transient changes
        child.addChild("foo");
        child.setIconName("iconName");

        // serialize parent
        out.writeObject(parent);

        // deserialize parent
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Node parentNode = (Node) in.readObject();

        // test deserialized node
        assertEquals("parent", parentNode.getName());
        assertEquals(((ApiNode) parent).context.getState(), ((ApiNode) parentNode).context.getState());
        assertTrue(parentNode.isChildrenLoaded());
        Node childNode = parentNode.getChild("child");
        assertNotNull(childNode);
        assertEquals(((ApiNode) child).context.getState(), ((ApiNode) childNode).context.getState());

        // test parent relationship
        assertNotNull(childNode.getParent());
        assertEquals("parent", childNode.getParent().getName());

        // test transient changes
        Node fooNode = childNode.getChild("foo");
        assertNotNull(fooNode);
        assertEquals(((ApiNode) child.getChild("foo")).context.getState(), ((ApiNode) fooNode).context.getState());

        // test object identities
        assertTrue(childNode == fooNode.getParent());
        assertTrue(parentNode.getParent() == childNode.getParent().getParent());
    }

    @Test
    public void serialization_multipath() throws Exception {
        createNavigationChildren();
        Node root = navigation.getRootNode(Nodes.visitAll());
        Node parent = root.getChild("parent");
        Node foo = parent.addChild("foo");
        foo.addChild("bar");
        Node child = parent.getChild("child");
        child.addChild("another-child");

        navigation.saveNode(root);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(root);

        // deserialize parent
        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray()));
        Node rootNode = (Node) in.readObject();
        assertNotNull(rootNode.getNode("parent", "foo", "bar"));
        assertNotNull(rootNode.getNode("parent", "child", "another-child"));
    }

    // TODO: Add more serialization tests like moving/removing nodes, displayName, etc
}
