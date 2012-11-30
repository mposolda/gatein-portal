/*
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

package org.exoplatform.commons.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.exoplatform.component.test.AbstractGateInTest;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestPageListAccess extends AbstractGateInTest {

    private final String[] l = { "0", "1", "2", "3", "4", "5", "6" };

    public void testPageList() throws Exception {
        StringPageListAccess pageList = new StringPageListAccess(3, l);

        //
        assertState1(pageList);

        //
        pageList = clone(pageList);
        assertState1(pageList);

        //
        List<String> list = pageList.getPage(1);
        assertListState1(list);
        assertState1(pageList);

        //
        pageList = clone(pageList);
        list = pageList.getPage(1);
        assertListState1(list);
        assertState1(pageList);

        //
        list = pageList.getPage(2);
        assertListState2(list);
        assertState2(pageList);

        //
        pageList = clone(pageList);
        list = pageList.getPage(2);
        assertListState2(list);
        assertState2(pageList);

        //
        list = pageList.getPage(3);
        assertListState3(list);
        assertState3(pageList);

        //
        pageList = clone(pageList);
        list = pageList.getPage(3);
        assertListState3(list);
        assertState3(pageList);
    }

    private void assertListState3(List<String> list) {
        assertNotNull(list);
        assertEquals(1, list.size());
        assertEquals("6", list.get(0));
    }

    private void assertState3(PageList<String> list) {
        assertEquals(6, list.getFrom());
        assertEquals(7, list.getTo());
        assertEquals(3, list.getCurrentPage());
        assertEquals(3, list.getAvailablePage());
        assertEquals(7, list.getAvailable());
    }

    private void assertState2(PageList<String> list) {
        assertEquals(3, list.getFrom());
        assertEquals(6, list.getTo());
        assertEquals(2, list.getCurrentPage());
        assertEquals(3, list.getAvailablePage());
        assertEquals(7, list.getAvailable());
    }

    private void assertListState2(List<String> s) {
        assertNotNull(s);
        assertEquals(3, s.size());
        assertEquals("3", s.get(0));
        assertEquals("4", s.get(1));
        assertEquals("5", s.get(2));
    }

    private void assertListState1(List<String> s) {
        assertNotNull(s);
        assertEquals(3, s.size());
        assertEquals("0", s.get(0));
        assertEquals("1", s.get(1));
        assertEquals("2", s.get(2));
    }

    private void assertState1(PageList<String> list) {
        assertEquals(0, list.getFrom());
        assertEquals(3, list.getTo());
        assertEquals(1, list.getCurrentPage());
        assertEquals(3, list.getAvailablePage());
        assertEquals(7, list.getAvailable());
    }

    private StringPageListAccess clone(StringPageListAccess pageList) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(pageList);
            oos.close();
            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (StringPageListAccess) ois.readObject();
        } catch (Exception e) {
            AssertionFailedError afe = new AssertionFailedError();
            afe.initCause(e);
            throw afe;
        }
    }

    public void testCorrectPageListSize() throws Exception {
        SizeTestPageListAccess testList = new SizeTestPageListAccess(10, "dummy");
        List<String> current = testList.getPage(1);
        current.get(0);

        // Page 1 (from index 0 of length 10) should be loaded at this point from underlying lazy List
        assertEquals(0, testList.lastLoadIndex);
        assertEquals(10, testList.lastLoadLength);

        // Setting new page size should recreate underlying lazy list
        testList.setPageSize(6);

        current = testList.getPage(7);
        current.get(0);

        // Page 7 (from index 36 of length 6) should be loaded at this point from underlying lazy list
        assertEquals(36, testList.lastLoadIndex);
        assertEquals(6, testList.lastLoadLength);
    }

    private static class SizeTestPageListAccess extends PageListAccess<String, String> {

        private int lastLoadIndex = -1;
        private int lastLoadLength = -1;

        public SizeTestPageListAccess(int pageSize, String state) {
            super(state, pageSize);
        }

        @Override
        protected ListAccess<String> create(String state) {
            return new ListAccess<String>() {

                public String[] load(int index, int length) {
                    lastLoadIndex = index;
                    lastLoadLength = length;

                    return new String[length];
                }

                public int getSize() {
                    return 50;
                }
            };
        }
    }
}
