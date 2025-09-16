package info.ejava.examples.svc.aop.items.services;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import info.ejava.examples.common.exceptions.ClientErrorException;
import info.ejava.examples.svc.aop.items.dto.ItemDTO;

public class ItemsServiceImpl<T extends ItemDTO> implements ItemsService<T> {

    private AtomicInteger nextId = new AtomicInteger(1);
    private  ConcurrentMap<Integer,T> items = new ConcurrentHashMap();
    private ObjectMapper mapper = new ObjectMapper();

    /**
     * Place a clone of the provided objects into storage since tests are all running within
     * the same JVM instance/thread. This prevents accidental instance reuse and easier to
     * make sense of post-test status.
     * @param original
     * @return copy of original
     */
    private T clone(T original) {
        try {
            String originalAsJson = mapper.writeValueAsString(original);
            return (T) mapper.readValue(originalAsJson, original.getClass());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("error cloning item", e);
        }
    }


    @Override
    public T createItem(T item) {
        T clone = clone(item);
        clone.setId(nextId.getAndAdd(1));
        items.put(clone.getId(), clone);
        return clone;
    }

    
    @Override
    public T updateItem(int id, T item) {
        if(!items.containsKey(id)){
            throw new ClientErrorException.NotFoundException("item[%d] not found", id);
        }

        T clone = clone(item);
        items.put(id, clone);
        clone.setId(id);
        return item;
    }

    @Override
    public T getItem(int id) {
        T item = items.get(id);
        if(item == null){
            throw new ClientErrorException.NotFoundException("item[%d] not found", id);
        }

        return item;
    }

    @Override
    public void deleteItem(int id) {
        items.remove(id);
    }

    @Override
    public void deleteItems() {
        items.clear();
    }
    
}
