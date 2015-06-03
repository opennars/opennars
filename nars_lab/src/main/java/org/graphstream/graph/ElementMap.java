/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphstream.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

/**
 * a sorted, memory-efficient map of elements
 *
 * @author bowen
 */
public class ElementMap<T extends Element> implements Iterable<T>
{
    private long lastCheck = System.currentTimeMillis();

    private final ArrayList<T> elements = new ArrayList<>(0);

    public void clear()
    {
        this.elements.clear();
        this.elements.trimToSize();
    }

    public boolean add(final T element)
    {
        if (null == element)
        {
            return false;
        }
        final int index = Collections.binarySearch(this.elements, element, ElementComparator.getInstance());
        if (index >= 0)
        {
            return false;
        }
        final int insertionPoint = -(index) - 1;
        this.elements.add(insertionPoint, element);
        this.checkAndTrim();
        return true;
    }

    public T get(final int index)
    {
        return this.elements.get(index);
    }

    public T get(final String id)
    {
        if (null == id)
        {
            return null;
        }
        final int index = Collections.binarySearch(this.elements, id, IdComparator.instance);
        if (index < 0)
        {
            return null;
        }
        return this.elements.get(index);
    }

    public boolean remove(final T element)
    {
        if (null == element)
        {
            return false;
        }
        final int index = Collections.binarySearch(this.elements, element, ElementComparator.getInstance());
        if (index < 0)
        {
            return false;
        }
        this.elements.remove(index);
        this.checkAndTrim();
        return true;
    }

    public boolean remove(final int index)
    {
        if (null == this.elements.remove(index))
        {
            return false;
        }
        this.checkAndTrim();
        return true;
    }

    public boolean remove(final String id)
    {
        if (null == id)
        {
            return false;
        }
        final int index = Collections.binarySearch(this.elements, id, IdComparator.instance);
        if (index < 0)
        {
            return false;
        }
        this.elements.remove(index);
        this.checkAndTrim();
        return true;
    }

    public int size()
    {
        return this.elements.size();
    }

    synchronized private boolean checkAndTrim()
    {
        final long delta = System.currentTimeMillis() - this.lastCheck;
        if (delta < 5000)
        {
            return false;
        }
        this.elements.trimToSize();
        this.lastCheck = System.currentTimeMillis();
        return true;
    }

    @Override
    public Iterator<T> iterator()
    {
        return this.elements.iterator();
    }

    private static class IdComparator implements Comparator
    {
        private static final IdComparator instance = new IdComparator();

        @Override
        public int compare(final Object left, final Object right)
        {
            final String leftId;
            if (left instanceof Element)
            {
                leftId = ((Element) left).getId();
            }
            else
            {
                leftId = left != null ? left.toString() : null;
            }

            final String rightId;
            if (right instanceof Element)
            {
                rightId = ((Element) right).getId();
            }
            else
            {
                rightId = right != null ? right.toString() : null;
            }

            if (null == leftId && null == rightId)
            {
                return 1;
            }
            else if (leftId != null)
            {
                return leftId.compareTo(rightId);
            }
            else
            {
                return -1;
            }
        }
    }
}
