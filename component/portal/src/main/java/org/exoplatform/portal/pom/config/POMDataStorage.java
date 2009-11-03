/**
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.portal.pom.config;

import org.exoplatform.commons.utils.IOUtil;
import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.portal.application.PortletPreferences;
import org.exoplatform.portal.config.Query;
import org.exoplatform.portal.config.model.Application;
import org.exoplatform.portal.config.model.ApplicationState;
import org.exoplatform.portal.config.model.Container;
import org.exoplatform.portal.config.model.ModelChange;
import org.exoplatform.portal.config.model.ModelObject;
import org.exoplatform.portal.pom.config.cache.DataCache;
import org.exoplatform.portal.pom.config.TaskExecutor;
import org.exoplatform.portal.pom.config.tasks.DashboardTask;
import org.exoplatform.portal.pom.data.DashboardData;
import org.exoplatform.portal.pom.data.ModelDataStorage;
import org.exoplatform.portal.pom.data.NavigationData;
import org.exoplatform.portal.pom.data.NavigationKey;
import org.exoplatform.portal.pom.data.PageData;
import org.exoplatform.portal.config.model.PersistentApplicationState;
import org.exoplatform.portal.pom.data.PageKey;
import org.exoplatform.portal.pom.data.PortalData;
import org.exoplatform.portal.pom.data.PortalKey;
import org.exoplatform.portal.config.model.TransientApplicationState;
import org.exoplatform.portal.pom.config.tasks.PageNavigationTask;
import org.exoplatform.portal.pom.config.tasks.PageTask;
import org.exoplatform.portal.pom.config.tasks.PortalConfigTask;
import org.exoplatform.portal.pom.config.tasks.PortletPreferencesTask;
import org.exoplatform.portal.pom.config.tasks.PreferencesTask;
import org.exoplatform.portal.pom.config.tasks.SearchTask;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;

import java.io.ByteArrayInputStream;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class POMDataStorage implements ModelDataStorage, ModelDemarcation
{

   /** . */
   private final POMSessionManager pomMgr;

   /** . */
   private ConfigurationManager confManager_;

   /** . */
   private final Log log = ExoLogger.getLogger(getClass());
   
   /** . */
   private final TaskExecutor executor;

   public POMDataStorage(CacheService cacheService, POMSessionManager pomMgr, ConfigurationManager confManager)
   {
      this.pomMgr = pomMgr;
      this.confManager_ = confManager;
      this.executor = new DataCache(cacheService, new ExecutorDispatcher(pomMgr));
   }

   public POMSessionManager getPOMSessionManager()
   {
      return pomMgr;
   }

   public PortalData getPortalConfig(PortalKey key) throws Exception
   {
      return executor.execute(new PortalConfigTask.Load(key)).getConfig();
   }

   public void create(PortalData config) throws Exception
   {
      executor.execute(new PortalConfigTask.Save(config, true));
   }

   public void save(PortalData config) throws Exception
   {
      executor.execute(new PortalConfigTask.Save(config, true));
   }

   public void remove(PortalData config) throws Exception
   {
      executor.execute(new PortalConfigTask.Remove(config.getKey()));
   }

   public PageData getPage(PageKey key) throws Exception
   {
      return executor.execute(new PageTask.Load(key)).getPage();
   }

   public PageData clonePage(PageKey key, PageKey cloneKey)
      throws Exception
   {
      return executor.execute(new PageTask.Clone(key, cloneKey, true)).getPage();
   }

   public void remove(PageData page) throws Exception
   {
      executor.execute(new PageTask.Remove(page));
   }

   public void create(PageData page) throws Exception
   {
      executor.execute(new PageTask.Save(page));
   }

   public List<ModelChange> save(PageData page) throws Exception
   {
      return executor.execute(new PageTask.Save(page)).getChanges();
   }

   public NavigationData getPageNavigation(NavigationKey key) throws Exception
   {
      return executor.execute(new PageNavigationTask.Load(key)).getPageNavigation();
   }

   public void save(NavigationData navigation) throws Exception
   {
      executor.execute(new PageNavigationTask.Save(navigation, true));
   }

   public void create(NavigationData navigation) throws Exception
   {
      executor.execute(new PageNavigationTask.Save(navigation, false));
   }

   public void remove(NavigationData navigation) throws Exception
   {
      executor.execute(new PageNavigationTask.Remove(navigation));
   }

   public void save(PortletPreferences portletPreferences) throws Exception
   {
      executor.execute(new PortletPreferencesTask.Save(portletPreferences));
   }

   public <S> S load(ApplicationState<S> state) throws Exception
   {
      if (state instanceof TransientApplicationState)
      {
         TransientApplicationState<S> transientState = (TransientApplicationState<S>)state;
         S prefs = transientState.getContentState();
         return prefs != null ? prefs : null;
      }
      else
      {
         PreferencesTask.Load<S> load = new PreferencesTask.Load<S>((PersistentApplicationState<S>)state);
         executor.execute(load);
         return load.getState();
      }
   }

   public <S> ApplicationState<S> save(ApplicationState<S> state, S preferences) throws Exception
   {
      if (state instanceof TransientApplicationState)
      {
         throw new AssertionError("Does not make sense");
      }
      else
      {
         PreferencesTask.Save<S> save = new PreferencesTask.Save<S>((PersistentApplicationState<S>)state, preferences);
         executor.execute(save);
         return state;
      }
   }

   public PortletPreferences getPortletPreferences(String windowID) throws Exception
   {
      return executor.execute(new PortletPreferencesTask.Load(windowID)).getPreferences();
   }

   public <T> LazyPageList<T> find(Query<T> q) throws Exception
   {
      return find(q, null);
   }

   public <T> LazyPageList<T> find(Query<T> q, Comparator<T> sortComparator) throws Exception
   {
      Class<T> type = q.getClassType();
      if (PageData.class.equals(type))
      {
         return (LazyPageList<T>)executor.execute(new SearchTask.FindPage((Query<PageData>)q)).getResult();
      }
      else if (NavigationData.class.equals(type))
      {
         return (LazyPageList<T>)executor.execute(new SearchTask.FindNavigation((Query<NavigationData>)q)).getResult();
      }
      else if (PortletPreferences.class.equals(type))
      {
         return (LazyPageList<T>)executor.execute(new SearchTask.FindPortletPreferences((Query<PortletPreferences>)q)).getResult();
      }
      else if (PortalData.class.equals(type))
      {
         return (LazyPageList<T>)executor.execute(new SearchTask.FindSite((Query<PortalData>)q)).getResult();
      }
      else if (PortalKey.class.equals(type) && "portal".equals(q.getOwnerType()))
      {
         return (LazyPageList<T>)executor.execute(new SearchTask.FindSiteKey((Query<PortalKey>)q)).getResult();
      }
      else
      {
         throw new UnsupportedOperationException("Could not perform search on query " + q);
      }
   }

   /**
    * This is a hack and should be removed, it is only used temporarily.
    * This is because the objects are loaded from files and don't have name.
    */
   private void generateStorageName(ModelObject obj)
   {
      if (obj instanceof Container)
      {
         for (ModelObject child : ((Container)obj).getChildren())
         {
            generateStorageName(child);
         }
      }
      else if (obj instanceof Application)
      {
         ((Application)obj).setStorageName(UUID.randomUUID().toString());
      }
   }

   public DashboardData loadDashboard(String dashboardId) throws Exception
   {
      return executor.execute(new DashboardTask.Load(dashboardId)).getDashboard();
   }

   public void saveDashboard(DashboardData dashboard) throws Exception
   {
      executor.execute(new DashboardTask.Save(dashboard));
   }

   public Container getSharedLayout() throws Exception
   {
      String path = "war:/conf/portal/portal/sharedlayout.xml";
      String out = IOUtil.getStreamContentAsString(confManager_.getInputStream(path));
      ByteArrayInputStream is = new ByteArrayInputStream(out.getBytes("UTF-8"));
      IBindingFactory bfact = BindingDirectory.getFactory(Container.class);
      IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
      Container container = Container.class.cast(uctx.unmarshalDocument(is, null));
      generateStorageName(container);
      return container;
   }

   public void begin()
   {
      getPOMSessionManager().openSession();
   }

   public void end(boolean save)
   {
      getPOMSessionManager().closeSession(save);
   }
}
