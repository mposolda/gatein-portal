/*
 * JBoss, a division of Red Hat
 * Copyright 2013, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.common.exception;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class GateInException extends RuntimeException {
    static final long serialVersionUID = -7034897190745768339L;

    // Specify error code
    private final int exceptionCode;

    // Context with additional attributes about error
    private final Map<String, Object> exceptionAttributes;

    public GateInException() {
        super();
        this.exceptionCode = GateInExceptionConstants.EXCEPTION_CODE_UNSPECIFIED;
        this.exceptionAttributes = new HashMap<String, Object>();
    }

    public GateInException(int exceptionCode, Map<String, Object> exceptionAttributes, String message) {
        super(message);
        this.exceptionCode = exceptionCode;
        this.exceptionAttributes = exceptionAttributes == null ? new HashMap<String, Object>() : exceptionAttributes;
    }

    public GateInException(int exceptionCode, Map<String, Object> exceptionAttributes, String message, Throwable cause) {
        super(message, cause);
        this.exceptionCode = exceptionCode;
        this.exceptionAttributes = exceptionAttributes == null ? new HashMap<String, Object>() : exceptionAttributes;
    }

    public GateInException(int exceptionCode, Map<String, Object> exceptionAttributes, Throwable cause) {
        super(cause);
        this.exceptionCode = exceptionCode;
        this.exceptionAttributes = exceptionAttributes == null ? new HashMap<String, Object>() : exceptionAttributes;
    }

    public int getExceptionCode() {
        return exceptionCode;
    }

    public Map<String, Object> getExceptionAttributes() {
        return Collections.unmodifiableMap(exceptionAttributes);
    }

    public Object getExceptionAttribute(String attrName) {
        return exceptionAttributes.get(attrName);
    }

}