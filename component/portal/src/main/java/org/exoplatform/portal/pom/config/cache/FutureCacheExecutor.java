package org.exoplatform.portal.pom.config.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.exoplatform.commons.cache.future.FutureCache;
import org.exoplatform.commons.cache.future.FutureExoCache;
import org.exoplatform.commons.cache.future.Loader;
import org.exoplatform.commons.scope.ScopedKey;
import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.commons.utils.ListAccessImpl;
import org.exoplatform.portal.pom.config.POMSession;
import org.exoplatform.portal.pom.config.POMTask;
import org.exoplatform.portal.pom.config.TaskExecutionDecorator;
import org.exoplatform.portal.pom.config.TaskExecutor;
import org.exoplatform.portal.pom.config.tasks.PortalConfigTask;
import org.exoplatform.portal.pom.config.tasks.SearchTask;
import org.exoplatform.portal.pom.data.PortalKey;
import org.exoplatform.services.cache.ExoCache;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class FutureCacheExecutor extends DataCache {

    private final Logger log = LoggerFactory.getLogger(FutureCacheExecutor.class);
    private final FutureCache<ScopedKey<?>, Object, FutureCacheContext> mopFutureCache;

    public FutureCacheExecutor(ExoCache<ScopedKey<?>, Object> cache, TaskExecutor next) {
        super(next);
        this.mopFutureCache = new FutureExoCache<ScopedKey<?>, Object, FutureCacheContext>(loader, cache);
    }

    @Override
    public <V> V execute(POMSession session, POMTask<V> task) throws Exception {
        if (!session.isModified()) {
            if (task instanceof SearchTask.FindSiteKey) {
                SearchTask.FindSiteKey find = (SearchTask.FindSiteKey) task;
                List<PortalKey> data = (List<PortalKey>) mopFutureCache.get(new FutureCacheContext(session, task), ScopedKey.create(find.getKey()));
                return (V) new LazyPageList<PortalKey>(new ListAccessImpl<PortalKey>(PortalKey.class, data), 10);
            }
        }

        if (task instanceof CacheableDataTask) {
            CacheableDataTask<?, V> loadTask = (CacheableDataTask<?, V>) task;
            V result; // super.execute(session, task);
            switch (loadTask.getAccessMode()) {
                case READ:
                    result = read(session, loadTask);
                    break;
                case CREATE:
                    result = create(session, loadTask);
                    break;
                case WRITE:
                    result = write(session, loadTask);
                    break;
                case DESTROY:
                    result = remove(session, loadTask);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }

            if ((!session.isModified()) && (task instanceof PortalConfigTask.Save || task instanceof PortalConfigTask.Remove)) {
                session.scheduleForEviction(SearchTask.FindSiteKey.PORTAL_KEY);
                session.scheduleForEviction(SearchTask.FindSiteKey.GROUP_KEY);
                return result;
            }
        }

        //
        return super.executeParent(session, task);
    }

    protected <K extends Serializable, V> V read(POMSession session, CacheableDataTask<K, V> task) throws Exception {
        K key = task.getKey();

        //
        if (!session.isModified()) {
            Object o = session.getFromCache(key);
            if (log.isTraceEnabled()) {
                log.trace("Retrieved " + o + " for key " + key);
            }

            V v = null;
            if (o != null) {
                if (o == NullObject.get()) {
                    if (log.isTraceEnabled()) {
                        log.trace("Returning null as found null object marker");
                    }
                    return null;
                } else {
                    Class<V> type = task.getValueType();
                    if (type.isInstance(o)) {
                        v = type.cast(o);
                    } else {
                        log.error("Object " + o + " was not of the expected type " + type);
                    }
                }
            }

            //
            if (v != null) {
                if (log.isTraceEnabled()) {
                    log.trace("Returning object " + v + " for key " + key);
                }
                return v;
            } else {
                readCount.incrementAndGet();

                //
                if (log.isTraceEnabled()) {
                    log.trace("Object not found in cache for key " + key + " about to retrieve it");
                }

                //
                v = super.execute(session, task);
                if (log.isTraceEnabled()) {
                    log.trace("Retrieved object " + v + " key " + key + " that will be returned");
                }

                //
                if (!session.isModified()) {
                    if (v == null) {
                        if (log.isTraceEnabled()) {
                            log.trace("Updating cache with null object for key " + key);
                        }
                        session.putInCache(key, NullObject.get());
                    } else {
                        if (log.isTraceEnabled()) {
                            log.trace("Updating cache with object " + v + " for key " + key);
                        }
                        session.putInCache(key, v);
                    }
                }

                //
                return v;
            }
        } else {
            if (log.isTraceEnabled()) {
                log.trace("Session was modified, object for key " + key + " is directly retrieved");
            }

            //
            return executeParent(session, task);
        }
    }


    private class FutureCacheContext {
        private POMSession session;
        private POMTask<? extends Object> task;

        private FutureCacheContext(POMSession session, POMTask<? extends Object> task) {
            this.session = session;
            this.task = task;
        }
    }

    private Loader<ScopedKey<?>, Object, FutureCacheContext> loader = new Loader<ScopedKey<?>, Object, FutureCacheContext>() {

        @Override
        public Object retrieve(FutureCacheContext context, ScopedKey<?> key) throws Exception {
            POMTask<?> task = context.task;
            POMSession session = context.session;

            if (task instanceof SearchTask.FindSiteKey) {
                LazyPageList<PortalKey> list = (LazyPageList<PortalKey>) FutureCacheExecutor.super.execute(session, task);
                return Collections.unmodifiableList(new ArrayList<PortalKey>(list.getAll()));
            } else {

            }
        }
    };
}
