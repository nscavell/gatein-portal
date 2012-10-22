package org.gatein.api.impl.portal.navigation;

import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.portal.navigation.NodeVisitor;

public class NodePathVisitor implements NodeVisitor {
    private NodePath nodePath;
    private boolean includeChildren;

    public NodePathVisitor(NodePath nodePath, boolean includeChildren) {
        this.nodePath = nodePath;
        this.includeChildren = includeChildren;
    }

    @Override
    public boolean visit(int depth, Node node) {
        if (depth == 0) {
            return true;
        } else if (depth < nodePath.size() || (depth == nodePath.size() && includeChildren)) {
            return nodePath.getSegment(depth - 1).equals(node.getName());
        } else {
            return false;
        }
    }
}