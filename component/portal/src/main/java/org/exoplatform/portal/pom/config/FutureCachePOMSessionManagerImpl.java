package org.exoplatform.portal.pom.config;

import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.portal.pom.config.cache.FutureCacheExecutor;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class FutureCachePOMSessionManagerImpl extends POMSessionManagerImpl {

    public FutureCachePOMSessionManagerImpl(RepositoryService repositoryService, ChromatticManager manager, CacheService cacheService) {
        super(repositoryService, manager, cacheService);
        TaskExecutionDecorator executor = new FutureCacheExecutor(getCache(), new ExecutorDispatcher());
        super.setTaskExecutor(executor);
    }


}
