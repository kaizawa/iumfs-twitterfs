package com.cafeform.iumfs.twitterfs;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author kaizawa
 * @param <E>
 */
public class ReEnterableListQueue<E> implements Queue<E>
{
    private final Queue<String> nameQueue = new LinkedBlockingQueue<>();
    private final Map<String, E> elementMap = new ConcurrentHashMap<>();

    @Override
    public boolean add (E element)
    {
        elementMap.put(element.toString(), element);
        return nameQueue.add(element.toString());        
    }

    @Override
    public boolean offer (E element)
    {
        elementMap.put(element.toString(), element);
        return nameQueue.offer(element.toString()); 
    }

    @Override
    public E remove ()
    {
        String elementName =  nameQueue.remove();        
        return elementMap.remove(elementName);
    }

    @Override
    public E poll ()
    {
        String elementName =  nameQueue.poll();
        if (null == elementName) {
            return null;
        }
        return elementMap.get(elementName);
    }

    @Override
    public E element ()
    {
        String elementName =  nameQueue.element();
        return elementMap.get(elementName);
    }

    @Override
    public E peek ()
    {
        String elementName =  nameQueue.element();
        return elementMap.get(elementName);    }

    @Override
    public int size ()
    {
        return elementMap.size();
    }

    @Override
    public boolean isEmpty ()
    {
        return elementMap.isEmpty();
    }

    @Override
    public boolean contains (Object o)
    {
        return elementMap.containsKey(o.toString());
    }

    @Override
    public Iterator<E> iterator ()
    {
        return elementMap.values().iterator();
    }

    @Override
    public Object[] toArray ()
    {
        return elementMap.values().toArray();
    }

    @Override
    public <T> T[] toArray (T[] a)
    {
        return elementMap.values().toArray(a);
    }

    @Override
    synchronized public boolean remove (Object o)
    {
        if ( elementMap.containsKey(o.toString()) &&
                nameQueue.contains(o.toString()))
        {
            elementMap.remove(o.toString());
            nameQueue.remove(o.toString());
            return true;
        }
        else 
        {
            return false;
        }
    }

    @Override
    public boolean containsAll (Collection<?> collection)
    {
        for (Object o : collection)
        {
            if (!contains(o))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll (Collection<? extends E> collection)
    {
        for (E o : collection)
        {
            if (!add(o))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean removeAll (Collection<?> collection)
    {
        for (Object o : collection)
        {
            @SuppressWarnings("unchecked")
            E e = (E)o;
            if (!remove(e))
            {
                return false;
            }
        }
        return true; 
    }

    @Override
    public boolean retainAll (Collection<?> collection)
    {
        for (E element : elementMap.values())
        {
            if (!collection.contains(element)) {
                if(!remove(element))
                {
                    return false;
                }
            } 
        }
        return true; 
    }

    @Override
    public void clear ()
    {
       elementMap.clear();
       nameQueue.clear();
    }
}
