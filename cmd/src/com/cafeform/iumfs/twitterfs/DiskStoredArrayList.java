package com.cafeform.iumfs.twitterfs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import static java.nio.file.StandardOpenOption.WRITE;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;
import static java.util.logging.Level.*;
import java.util.logging.Logger;

/**
 *
 */
public class DiskStoredArrayList<T> implements List<T>, Serializable
{

    static final Logger logger = Logger.getLogger(DiskStoredArrayList.class.getName());
    private SoftReference<CopyOnWriteArrayList<T>> reference;
    private final Path backupFile;

    public DiskStoredArrayList (String pathName, boolean useBackup)
            throws IOException
    {
        backupFile = Paths.get(pathName);
        Files.createDirectories(backupFile.getParent());
        CopyOnWriteArrayList<T> arrayList;

        if (useBackup && Files.exists(backupFile))
        {
            arrayList = readFile();
        } else
        {
            Files.deleteIfExists(backupFile);
            Files.createFile(backupFile);
            // Create initial instance            
            arrayList = new CopyOnWriteArrayList<>();
        }
        reference = new SoftReference<>(arrayList);
        writeFile(arrayList);
    }

    /**
     * Get instance of CopyOnWriteArrayList.
     */
    private CopyOnWriteArrayList<T> getInstance ()
    {
        CopyOnWriteArrayList<T> arrayList;

        if (null == (arrayList = reference.get()))
        {
            arrayList = readFile();
            logger.log(FINER, "Deserialized " + backupFile.getFileName());
            reference = new SoftReference<>(arrayList);
        }
        return arrayList;
    }

    synchronized private CopyOnWriteArrayList<T> readFile ()
    {
        // Insatnce is gone. deserialize from file.
        try (ObjectInputStream inputStream
                = new ObjectInputStream(Files.newInputStream(backupFile)))
        {
            @SuppressWarnings("unchecked")
            CopyOnWriteArrayList<T> arrayList
                    = (CopyOnWriteArrayList<T>) inputStream.readObject();
            return arrayList;
        } catch (OptionalDataException ex)
        {
            logger.log(WARNING, "Backup timeline file " + 
                    backupFile.getFileName() + " is corrupted. Recreating.");
            try
            {
                Files.deleteIfExists(backupFile);
                Files.createFile(backupFile);
            } catch (IOException exi)
            {
                throw new IllegalStateException("Cannot re-create " + 
                        backupFile.getFileName(),
                        exi);
            }
            // Create initial instance            
            return new CopyOnWriteArrayList<>();
        } 
        catch (IOException | ClassNotFoundException ex)
        {
            throw new IllegalStateException("Cannot deserialize "
                    + backupFile.getFileName() + ".", ex);
        }
    }

    synchronized private void writeFile (CopyOnWriteArrayList<T> arrayList)
    {
        try (ObjectOutputStream outputStream = new ObjectOutputStream(
                Files.newOutputStream(backupFile, WRITE));)
        {
            outputStream.writeObject(arrayList);
        } catch (IOException ex)
        {
            throw new IllegalStateException("Cannot serialize "
                    + backupFile.getFileName() + ".", ex);
        }
    }

    @Override
    public int size ()
    {
        return getInstance().size();
    }

    @Override
    public boolean isEmpty ()
    {
        return getInstance().isEmpty();
    }

    @Override
    public boolean contains (Object o)
    {
        return getInstance().contains(o);
    }

    @Override
    public Iterator<T> iterator ()
    {
        return getInstance().iterator();
    }

    @Override
    public Object[] toArray ()
    {
        return getInstance().toArray();
    }

    @Override
    public <T> T[] toArray (T[] a)
    {
        return getInstance().toArray(a);
    }

    @Override
    public boolean add (T e)
    {
        CopyOnWriteArrayList<T> arrayList = getInstance();
        boolean result = arrayList.add(e);
        writeFile(arrayList);
        return result;
    }

    @Override
    public boolean remove (Object o)
    {
        CopyOnWriteArrayList<T> arrayList = getInstance();
        boolean result = arrayList.remove(o);
        writeFile(arrayList);
        return result;
    }

    @Override
    public boolean containsAll (Collection<?> c)
    {
        return getInstance().containsAll(c);
    }

    @Override
    public boolean addAll (Collection<? extends T> c)
    {
        CopyOnWriteArrayList<T> arrayList = getInstance();
        boolean result = arrayList.addAll(c);
        writeFile(arrayList);
        return result;
    }

    @Override
    public boolean addAll (int index, Collection<? extends T> c)
    {
        CopyOnWriteArrayList<T> arrayList = getInstance();
        boolean result = arrayList.addAll(index, c);
        writeFile(arrayList);
        return result;
    }

    @Override
    public boolean removeAll (Collection<?> c)
    {
        CopyOnWriteArrayList<T> arrayList = getInstance();
        boolean result = arrayList.removeAll(c);
        writeFile(arrayList);
        return result;
    }

    @Override
    public boolean retainAll (Collection<?> c)
    {
        CopyOnWriteArrayList<T> arrayList = getInstance();
        boolean result = arrayList.retainAll(c);
        writeFile(arrayList);
        return result;
    }

    @Override
    public void clear ()
    {
        CopyOnWriteArrayList<T> arrayList = getInstance();
        arrayList.clear();
        writeFile(arrayList);
    }

    @Override
    public T get (int index)
    {
        return getInstance().get(index);
    }

    @Override
    public T set (int index, T element)
    {
        CopyOnWriteArrayList<T> arrayList = getInstance();
        T result = arrayList.set(index, element);
        writeFile(arrayList);
        return result;
    }

    @Override
    public void add (int index, T element)
    {
        CopyOnWriteArrayList<T> arrayList = getInstance();
        arrayList.add(index, element);
        writeFile(arrayList);
    }

    @Override
    public T remove (int index)
    {
        CopyOnWriteArrayList<T> arrayList = getInstance();
        T result = arrayList.remove(index);
        writeFile(arrayList);
        return result;
    }

    @Override
    public int indexOf (Object o)
    {
        return getInstance().indexOf(o);
    }

    @Override
    public int lastIndexOf (Object o)
    {
        return getInstance().lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator ()
    {
        return getInstance().listIterator();
    }

    @Override
    public ListIterator<T> listIterator (int index)
    {
        return getInstance().listIterator(index);
    }

    @Override
    public List<T> subList (int fromIndex, int toIndex)
    {
        return getInstance().subList(fromIndex, toIndex);
    }

}
