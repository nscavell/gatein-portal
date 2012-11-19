package org.gatein.api.impl;

import java.io.InputStream;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.mop.description.DescriptionService;
import org.exoplatform.portal.mop.navigation.NavigationService;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMSessionManager;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.resources.ResourceBundleManager;
import org.gatein.api.portal.navigation.Node;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.osgi.spi.ManifestBuilder;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.Filter;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TmpPortalIT
{

   private PortalImpl portal;
   private POMSession session;

   // @Resource(mappedName = "java:jboss/UserTransaction")
   // private UserTransaction tx;
   private PortalContainer container;
   private NavigationService navService;

   @Deployment
   public static JavaArchive createDeployment()
   {
      Filter<ArchivePath> filter = new Filter<ArchivePath>()
      {
         final Class<?>[] ignore = new Class[]
         { AbstractAPITestCase.class, PortalTestCase.class, SimpleRequestContext.class };

         @Override
         public boolean include(ArchivePath object)
         {
            for (Class<?> c : ignore)
            {
               String n = "/" + c.getName().replace('.', '/');
               if (object.get().startsWith(n))
               {
                  return false;
               }
            }
            return true;
         }
      };

      JavaArchive javaArchive = ShrinkWrap
            .create(JavaArchive.class)
            .addPackages(true, filter, "org.gatein.api", "org.gatein.api.impl", "org.gatein.api.internal",
                  "org.gatein.management.api", "org.gatein.api.util", "org.gatein.api.portal", "org.gatein.api.portal.page",
                  "org.gatein.api.portal.site", "org.gatein.api.portal.navigation", "org.gatein.api.impl.portal",
                  "org.gatein.api.impl.portal.navigation", "org.gatein.api.management", "org.gatein.api.management.portal",
                  "org.gatein.management.api.model").addClass(ManifestBuilder.class);

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

      javaArchive.addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");

      return javaArchive;
   }

   @Before
   public void setup()
   {
      container = PortalContainer.getInstance();

      POMSessionManager pomSession = (POMSessionManager) container.getComponentInstanceOfType(POMSessionManager.class);
      session = pomSession.openSession();
      DataStorage dataStorage = (DataStorage) container.getComponentInstanceOfType(DataStorage.class);
      navService = (NavigationService) container.getComponentInstanceOfType(NavigationService.class);
      DescriptionService descriptionService = (DescriptionService) container
            .getComponentInstanceOfType(DescriptionService.class);
      OrganizationService orgService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
      ResourceBundleManager bundleManager = (ResourceBundleManager) container
            .getComponentInstanceOfType(ResourceBundleManager.class);

      portal = new PortalImpl(dataStorage, navService, descriptionService);//, orgService, bundleManager);
   }

   @After
   public void close()
   {
      if (session != null)
      {
         session.close();
      }
   }

   private void printNodeTree(Node n)
   {
      while (n.getParent() != null)
      {
         n = n.getParent();
      }
      printNodeTree(n.getChildren(), 0);
   }

   private void printNodeTree(List<Node> nodes, int depth)
   {
      for (Node n : nodes)
      {
         for (int i = 0; i < depth; i++)
         {
            System.out.print("  ");
         }
         System.out.println(n.getName() + " " + n.getLabel());
         printNodeTree(n.getChildren(), depth + 1);
      }
   }
}
