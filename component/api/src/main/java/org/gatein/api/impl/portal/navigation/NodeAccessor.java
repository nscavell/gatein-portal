package org.gatein.api.impl.portal.navigation;

import java.lang.reflect.Field;
import java.util.List;

import org.gatein.api.portal.navigation.Node;

public class NodeAccessor {

    private static Field parent;
    
    private static Field children;
    
    static {
        try {
            parent = Node.class.getDeclaredField("parent");
            parent.setAccessible(true);
            
            children = Node.class.getDeclaredField("children");
            children.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setChildren(Node node, List<Node> children) {
        try {
            NodeAccessor.children.set(node, children);
            if (children != null) {
                for (Node child : children) {
                    NodeAccessor.parent.set(child, node);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
