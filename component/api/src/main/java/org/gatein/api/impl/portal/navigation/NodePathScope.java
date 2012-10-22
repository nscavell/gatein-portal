package org.gatein.api.impl.portal.navigation;

import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.navigation.VisitMode;
import org.gatein.api.portal.navigation.NodePath;

public class NodePathScope implements Scope {

    private final NodePathVisitor nodePathVisitor;

    public NodePathScope(NodePath nodePath) {
        nodePathVisitor = new NodePathVisitor(nodePath);
    }

    @Override
    public Visitor get() {
        return nodePathVisitor;
    }

    public static class NodePathVisitor implements Visitor {
        private final NodePath nodePath;

        public NodePathVisitor(NodePath nodePath) {
            this.nodePath = nodePath;
        }

        @Override
        public VisitMode enter(int depth, String id, String name, NodeState state) {
            if (depth == 0 ) {
                return VisitMode.ALL_CHILDREN;
            } else if (depth < nodePath.size() && nodePath.getSegment(depth - 1).equals(name)) {
                return VisitMode.ALL_CHILDREN;
            } else {
                return VisitMode.NO_CHILDREN;
            }
        }

        @Override
        public void leave(int depth, String id, String name, NodeState state) {
        }
    }
}
