package org.gatein.api.impl.portal.navigation;

import org.exoplatform.portal.mop.navigation.NodeState;
import org.exoplatform.portal.mop.navigation.Scope;
import org.exoplatform.portal.mop.navigation.VisitMode;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodeVisitor;

public class NodeVisitorScope implements Scope {

    private final NodeVisitorWrapper nodePathVisitor;

    public NodeVisitorScope(NodeVisitor nodeVisitor) {
        nodePathVisitor = new NodeVisitorWrapper(nodeVisitor);
    }

    @Override
    public Visitor get() {
        return nodePathVisitor;
    }

    public static class NodeVisitorWrapper implements Visitor {
        private final NodeVisitor nodeVisitor;

        public NodeVisitorWrapper(NodeVisitor nodeVisitor) {
            this.nodeVisitor = nodeVisitor;
        }

        /**
         * TODO Would be easier to implement if NodeVisitor only sees Node name, not Node itself; otherwise the implementation
         * will probably not be very efficient
         */
        @Override
        public VisitMode enter(int depth, String id, String name, NodeState state) {
            if (nodeVisitor.visit(depth, new Node(name))) {
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
