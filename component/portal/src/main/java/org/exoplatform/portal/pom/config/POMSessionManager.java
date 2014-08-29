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

import java.io.Serializable;

import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.gatein.mop.core.api.MOPService;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface POMSessionManager {

    public ChromatticLifeCycle getLifeCycle();

    public void cachePut(Serializable key, Object value);

    public Object cacheGet(Serializable key);

    public void cacheRemove(Serializable key);

    public void clearCache();

    public MOPService getPOMService();

    public <E extends TaskExecutionDecorator> E getDecorator(Class<E> decoratorClass);

    /**
     * <p>
     * Returns the session currently associated with the current thread of execution.
     * </p>
     *
     * @return the current session
     */
    public POMSession getSession();

    /**
     * <p>
     * Open and returns a session to the model. When the current thread is already associated with a previously opened session
     * the method will throw an <tt>IllegalStateException</tt>.
     * </p>
     *
     * @return a session to the model.
     */
    public POMSession openSession();

    /**
     * <p>
     * Execute the task with a session.
     * </p>
     *
     * @param task the task to execute
     * @throws Exception any exception thrown by the task
     * @return the value
     */
    public <V> V execute(POMTask<V> task) throws Exception;

}
