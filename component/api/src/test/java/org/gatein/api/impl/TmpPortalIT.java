package org.gatein.api.impl;

import java.io.InputStream;
import java.util.UUID;

import junit.framework.Assert;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.resources.ResourceBundleManager;
import org.gatein.api.portal.Ids;
import org.gatein.api.portal.navigation.Navigation;
import org.gatein.api.portal.navigation.Node;
import org.gatein.api.portal.navigation.NodePath;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.osgi.spi.ManifestBuilder;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@RunWith(Arquillian.class)
public class TmpPortalIT
{

   private PortalImpl portal;
   private POMSession session;

   @Deployment
   public static JavaArchive createDeployment()
   {
      JavaArchive javaArchive = ShrinkWrap.create(JavaArchive.class).addClass(TmpPortalIT.class).addPackage("org.gatein.api")
            .addPackage("org.gatein.api.internal").addPackage("org.gatein.management.api").addPackage("org.gatein.api.util")
            .addPackage("org.gatein.api.portal").addPackage("org.gatein.api.portal.page")
            .addPackage("org.gatein.api.portal.site").addPackage("org.gatein.api.portal.navigation")
            .addPackage("org.gatein.api.impl").addPackage("org.gatein.api.impl.portal")
            .addPackage("org.gatein.api.impl.portal.navigation").addPackage("org.gatein.api.management")
            .addPackage("org.gatein.api.management.portal").addPackage("org.gatein.management.api.model");

      final ManifestBuilder mf = ManifestBuilder.newInstance();
      mf.addManifestHeader("Dependencies", "org.gatein.common, org.gatein.lib, org.picocontainer");
      javaArchive.setManifest(new Asset()
      {
         @Override
         public InputStream openStream()
         {
            return mf.openStream();
         }
      });

      return javaArchive;
   }

   @Before
   public void setup()
   {
      PortalContainer container = PortalContainer.getInstance();

      POMSessionManager pomSession = (POMSessionManager) container.getComponentInstanceOfType(POMSessionManager.class);
      session = pomSession.openSession();

      DataStorage dataStorage = (DataStorage) container.getComponentInstanceOfType(DataStorage.class);
      NavigationService navService = (NavigationService) container.getComponentInstanceOfType(NavigationService.class);
      DescriptionService descriptionService = (DescriptionService) container
            .getComponentInstanceOfType(DescriptionService.class);
      OrganizationService orgService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
      ResourceBundleManager bundleManager = (ResourceBundleManager) container
            .getComponentInstanceOfType(ResourceBundleManager.class);

      portal = new PortalImpl(dataStorage, navService, descriptionService, orgService, bundleManager);
   }

   @After
   public void close()
   {
      session.save();
      session.close();
   }

   @Test
   public void navigation()
   {
      Navigation navigation = portal.getNavigation(Ids.siteId("classic"), null, null);
      printNavigation(navigation);
   }

   private void printNavigation(Navigation navigation)
   {
      for (Node n : navigation.getNodes())
      {
         printNodeTree(n);
      }
   }

   @Test
   public void getNode()
   {
      Node node = portal.getNode(Ids.siteId("classic"), new NodePath("home", "Test2", "Test3"));

      printNodeTree(node);

      Assert.assertNotNull(node);
      Assert.assertEquals("Test3", node.getName());

      Assert.assertNull(node.getChildren());

      node = node.getParent();
      Assert.assertNotNull(node);
      Assert.assertEquals("Test2", node.getName());

      node = node.getParent();
      Assert.assertNotNull(node);
      Assert.assertEquals("home", node.getName());

      node = node.getParent();
      Assert.assertNull(node);
   }

   @Test
   public void addChild() throws Exception
   {
      Node homeNode = portal.getNode(Ids.siteId("classic"), new NodePath("home"));
      portal.loadNodes(homeNode, null);

      Node node2 = new Node(UUID.randomUUID().toString());
      node2.setPageId(homeNode.getPageId());

      homeNode.addChild(node2);
      // portal.saveNode(homeNode);
      portal.saveNode(node2);

      homeNode = portal.getNode(Ids.siteId("classic"), new NodePath("home"));
      portal.loadNodes(homeNode, null);
      printNodeTree(homeNode);
   }

   @Test
   public void removeChild() throws Exception
   {
      Node homeNode = portal.getNode(Ids.siteId("classic"), new NodePath("home"));
      portal.loadNodes(homeNode, null);

      homeNode.removeNode("Test");
      portal.saveNode(homeNode);
      printNodeTree(homeNode);

      System.out.println("XXXXX");

      homeNode = portal.getNode(Ids.siteId("classic"), new NodePath("home"));
      portal.loadNodes(homeNode, null);
      printNodeTree(homeNode);
   }

   private void printNodeTree(Node nc)
   {
      while (nc.getParent() != null)
      {
         nc = nc.getParent();
      }
      printNodeTree(nc, 0);
   }

   private void printNodeTree(Node nc, int depth)
   {
      for (int i = 0; i < depth; i++)
      {
         System.out.print("  ");
      }
      if (nc.getChildren() != null)
      {
         System.out.println(nc.getName());
         for (Node c : nc.getChildren())
         {
            printNodeTree(c, depth + 1);
         }
      }
      else
      {
         System.out.println(nc.getName() + " (children not loaded)");
      }
   }
}
