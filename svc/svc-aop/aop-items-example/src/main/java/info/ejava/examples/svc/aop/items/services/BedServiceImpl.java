package info.ejava.examples.svc.aop.items.services;

import org.springframework.stereotype.Service;

import info.ejava.examples.svc.aop.items.dto.BedDTO;
import info.ejava.examples.svc.aop.items.dto.ItemDTO;

@Service
public class BedServiceImpl extends ItemsServiceImpl<BedDTO> {

    @Override
    public BedDTO createItem(BedDTO item) {
        return super.createItem(item);
    }

    @Override
    public BedDTO updateItem(int id, BedDTO item) {
        return super.updateItem(id, item);
    }

    @Override
    public BedDTO getItem(int id) {
        return super.getItem(id);
    }

    @Override
    public void deleteItem(int id) {
        super.deleteItem(id);
    }

    @Override
    public void deleteItems() {
        super.deleteItems();
    }

        
}
