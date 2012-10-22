package org.gatein.api.impl.portal.navigation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.portal.mop.navigation.NavigationContext;
import org.exoplatform.portal.mop.navigation.NodeContext;
import org.exoplatform.portal.mop.navigation.NodeState;
import org.gatein.api.portal.navigation.Navigation;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodePath;
import org.gatein.api.portal.navigation.PublicationDate;
import org.gatein.api.portal.navigation.Visibility;
import org.gatein.api.portal.navigation.Visibility.Flag;
import org.gatein.api.portal.site.Site;

public class NavigationUtil {

    public static Node findNode(Navigation navigation, NodePath nodePath) {
        Iterator<String> itr = nodePath.iterator();
        List<Node> nodes = navigation.getNodes();
        String rootNodeName = itr.next();
        for (Node n : nodes) {
            if (n.getName().equals(rootNodeName)) {
                while (itr.hasNext()) {
                    n = n.getChild(itr.next());
                    if (n == null) {
                        return null; // TODO Do we throw exception or return null?
                    }
                }
                return n;
            }
        }
        return null; // TODO Do we throw exception or return null?
    }

    @SuppressWarnings("unchecked")
    public static NodeContext<NodeContext<?>> findNodeContext(NodeContext<NodeContext<?>> rootNodeCtx, NodePath nodePath) {
        Iterator<String> itr = nodePath.iterator();
        Collection<NodeContext<?>> nodes = rootNodeCtx.getNodes();
        String rootNodeName = itr.next();
        for (NodeContext<?> n : nodes) {
            if (n.getName().equals(rootNodeName)) {
                while (itr.hasNext()) {
                    n = n.get(itr.next());
                    if (n == null) {
                        return null; // TODO Do we throw exception or return null?
                    }
                }
                return (NodeContext<NodeContext<?>>) n;
            }
        }
        return null; // TODO Do we throw exception or return null?
    }

    private static Flag from(org.exoplatform.portal.mop.Visibility visibility) {
        switch (visibility) {
            case DISPLAYED:
                return Flag.VISIBLE;
            case HIDDEN:
                return Flag.HIDDEN;
            case SYSTEM:
                return Flag.SYSTEM;
            case TEMPORAL:
                return Flag.PUBLICATION;
            default:
                return null; // TODO SHould this throw an exception?
        }
    }

    public static org.exoplatform.portal.mop.Visibility from(Flag flag) {
        switch (flag) {
            case VISIBLE:
                return org.exoplatform.portal.mop.Visibility.DISPLAYED;
            case HIDDEN:
                return org.exoplatform.portal.mop.Visibility.HIDDEN;
            case SYSTEM:
                return org.exoplatform.portal.mop.Visibility.SYSTEM;
            case PUBLICATION:
                return org.exoplatform.portal.mop.Visibility.TEMPORAL;
            default:
                return null; // TODO SHould this throw an exception?
        }
    }

    public static NodeState from(Node node, NodeContext<?> nodeContext) {
        String label = nodeContext.getState().getLabel(); // TODO
        String icon = node.getIconName();

        PublicationDate publicationDate = node.getVisibility().getPublicationDate();

        long startPublicationTime = -1;
        long endPublicationTime = -1;

        if (publicationDate != null) {
            if (publicationDate.getStart() != null) {
                startPublicationTime = publicationDate.getStart().getTime();
            }
            
            if (publicationDate.getEnd() != null) {
                endPublicationTime = publicationDate.getEnd().getTime();
            }
        }

        org.exoplatform.portal.mop.Visibility visibility = from(node.getVisibility().getFlag());

        return new NodeState(label, icon, startPublicationTime, endPublicationTime, visibility, nodeContext.getState().getPageRef());
    }

    private static Visibility from(NodeState nodeState) {
        Flag flag = from(nodeState.getVisibility());

        long start = nodeState.getStartPublicationTime();
        long end = nodeState.getEndPublicationTime();

        PublicationDate publicationDate = null;
        if (start != -1 && end != -1) {
            publicationDate = PublicationDate.between(new Date(start), new Date(end));
        } else if (start != -1) {
            publicationDate = PublicationDate.startingOn(new Date(start));
        } else if (end != -1) {
            publicationDate = PublicationDate.endingOn(new Date(end));
        }

        return new Visibility(flag, publicationDate);
    }

    @SuppressWarnings("unchecked")
    private static List<Node> from(Site.Id siteId, Collection<NodeContext<?>> nodesInternal) {
        List<Node> nodes = new ArrayList<Node>();
        for (NodeContext<?> childNodeInternal : nodesInternal) {
            Visibility visibility = from(childNodeInternal.getState());

            List<Node> children = null;
            if (childNodeInternal.getNodes() != null) {
                children = from(siteId, (Collection<NodeContext<?>>) childNodeInternal.getNodes());
            }
            
            Node childNode = new Node(childNodeInternal.getName());
            childNode.setIconName(childNodeInternal.getState().getIcon());
            // TODO Set label on node
            // n.setLabel(label);
            childNode.setPageId(new org.gatein.api.portal.page.Page.Id(siteId, childNodeInternal.getName()));
            childNode.setVisibility(visibility);
            NodeAccessor.setChildren(childNode, children);
            
            nodes.add(childNode);
        }
        return nodes;
    }
    
    public static Navigation from(Site.Id siteId, NavigationContext navigationInternal,
            NodeContext<NodeContext<?>> rootNodeInternal) {
        Navigation navigation = new Navigation(siteId, navigationInternal.getState().getPriority());
        List<Node> nodes = from(siteId, rootNodeInternal.getNodes());
        navigation.setNodes(nodes);
        return navigation;
    }
}
