package org.graphstream.graph;

import java.util.Comparator;

/**
 * a simple comparator that sorts by element id
 * <p>
 * User: bowen
 * Date: 11/14/14
 */
public class ElementComparator implements Comparator<Element>
{
    private static final ElementComparator instance = new ElementComparator();


    public static Comparator<Element> getInstance()
    {
        return instance;
    }


    private ElementComparator()
    {

    }


    @Override
    public int compare(final Element o1, final Element o2)
    {
        if (o1 == o2)
        {
            return 0;
        }
        if (o1 == null)
        {
            return -1;
        }
        if (o2 == null)
        {
            return 1;
        }
        return o1.getId().compareTo(o2.getId());
    }
}
