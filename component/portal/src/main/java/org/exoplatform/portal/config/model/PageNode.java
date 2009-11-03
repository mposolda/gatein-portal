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

package org.exoplatform.portal.config.model;

import org.exoplatform.commons.utils.ExpressionUtil;
import org.exoplatform.portal.pom.data.NavigationNodeData;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

public class PageNode extends PageNodeContainer
{

   private ArrayList<PageNode> children;

   private String uri;

   private String label;

   private String icon;

   private String name;

   private String resolvedLabel;

   private Date startPublicationDate;

   private Date endPublicationDate;

   private boolean showPublicationDate = false;

   private boolean visible = true;

   private String pageReference;

   private transient boolean modifiable;

   public PageNode(NavigationNodeData nav)
   {
      super(nav.getStorageId());

      //
      ArrayList<PageNode> children = new ArrayList<PageNode>(nav.getNodes().size());
      for (NavigationNodeData child : nav.getNodes())
      {
         PageNode node = new PageNode(child);
         children.add(node);
      }

      //
      this.uri = nav.getURI();
      this.label = nav.getLabel();
      this.resolvedLabel = nav.getLabel();
      this.icon = nav.getIcon();
      this.name = nav.getName();
      this.startPublicationDate = nav.getStartPublicationDate();
      this.endPublicationDate = nav.getEndPublicationDate();
      this.showPublicationDate = nav.getShowPublicationDate();
      this.visible = nav.isVisible();
      this.pageReference = nav.getPageReference();
      this.children = children;
   }

   public PageNode(String storageId)
   {
      super(storageId);

      //
      this.children = new ArrayList<PageNode>();
   }

   public PageNode()
   {
      this((String)null);
   }

   public String getUri()
   {
      return uri;
   }

   public void setUri(String s)
   {
      uri = s;
   }

   public String getLabel()
   {
      return label;
   }

   public void setLabel(String s)
   {
      label = s;
      resolvedLabel = s;
   }

   public String getIcon()
   {
      return icon;
   }

   public void setIcon(String s)
   {
      icon = s;
   }

   public String getPageReference()
   {
      return pageReference;
   }

   public void setPageReference(String s)
   {
      pageReference = s;
   }

   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public String getResolvedLabel()
   {
      return resolvedLabel;
   }

   public void setResolvedLabel(String res)
   {
      resolvedLabel = res;
   }

   public void setResolvedLabel(ResourceBundle res)
   {
      resolvedLabel = ExpressionUtil.getExpressionValue(res, label);
      if (resolvedLabel == null)
         resolvedLabel = getName();
   }

   public List<PageNode> getChildren()
   {
      return children;
   }

   public void setChildren(ArrayList<PageNode> list)
   {
      children = list;
   }

   public boolean isModifiable()
   {
      return modifiable;
   }

   public void setModifiable(boolean b)
   {
      modifiable = b;
   }

   public Date getStartPublicationDate()
   {
      return startPublicationDate;
   }

   public void setStartPublicationDate(Date startDate)
   {
      startPublicationDate = startDate;
   }

   public Date getEndPublicationDate()
   {
      return endPublicationDate;
   }

   public void setEndPublicationDate(Date endDate)
   {
      endPublicationDate = endDate;
   }

   public boolean isDisplay()
   {
      if (visible && showPublicationDate)
      {
         return isInPublicationDate();
      }
      return visible;
   }

   public boolean isVisible()
   {
      return visible;
   }

   public boolean getVisible()
   {
      return visible;
   }

   public void setVisible(Boolean b)
   {
      visible = b.booleanValue();
   }

   private boolean isInPublicationDate()
   {
      if (startPublicationDate != null && endPublicationDate != null)
      {
         Date currentDate = new Date();
         if (currentDate.compareTo(startPublicationDate) >= 0 && currentDate.compareTo(endPublicationDate) <= 0)
            return true;
      }
      else if (startPublicationDate == null && endPublicationDate == null)
         return true;
      return false;
   }

   public void setShowPublicationDate(Boolean show)
   {
      showPublicationDate = show.booleanValue();
   }

   public boolean isShowPublicationDate()
   {
      return showPublicationDate;
   }

   public PageNode getChild(String name)
   {
      if (children == null)
         return null;
      for (PageNode node : children)
      {
         if (node.getName().equals(name))
            return node;
      }
      return null;
   }

   public List<PageNode> getNodes()
   {
      return children;
   }

   public PageNode clone()
   {
      PageNode newNode = new PageNode();
      newNode.setUri(uri);
      newNode.setLabel(label);
      newNode.setIcon(icon);
      newNode.setName(name);
      newNode.setResolvedLabel(resolvedLabel);
      newNode.setPageReference(pageReference);
      newNode.setModifiable(modifiable);
      newNode.setShowPublicationDate(showPublicationDate);
      newNode.setStartPublicationDate(startPublicationDate);
      newNode.setEndPublicationDate(endPublicationDate);
      newNode.setVisible(visible);
      if (children == null || children.size() < 1)
         return newNode;
      for (PageNode ele : children)
      {
         newNode.getChildren().add(ele.clone());
      }
      return newNode;
   }

   @Override
   public NavigationNodeData build()
   {
      List<NavigationNodeData> children = buildNavigationChildren();
      return new NavigationNodeData(
         storageId,
         uri,
         label,
         icon,
         name,
         startPublicationDate,
         endPublicationDate,
         showPublicationDate,
         visible,
         pageReference,
         children
      );
   }
}