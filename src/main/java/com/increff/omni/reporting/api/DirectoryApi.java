package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.DirectoryDao;
import com.increff.omni.reporting.pojo.DirectoryPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class DirectoryApi extends AbstractApi {

    @Autowired
    private DirectoryDao dao;

    public DirectoryPojo add(DirectoryPojo pojo) throws ApiException {
        validate(pojo);
        dao.persist(pojo);
        return pojo;
    }

    public DirectoryPojo getCheck(Integer id) throws ApiException {
        DirectoryPojo pojo = dao.select(id);
        checkNotNull(pojo, "No directory with id : " + id);
        return pojo;
    }

    public String getDirectoryPath(Integer id) throws ApiException {
        DirectoryPojo directoryPojo = getCheck(id);
        StringBuilder directoryPath = new StringBuilder(directoryPojo.getDirectoryName());
        Integer parentId = directoryPojo.getParentId();
        while (parentId != 0) {
            DirectoryPojo p = getCheck(parentId);
            directoryPath.insert(0, p.getDirectoryName() + "/");
            parentId = p.getParentId();
        }
        return directoryPath.toString();
    }

    public List<DirectoryPojo> getAll() {
        return dao.selectAll();
    }

    public DirectoryPojo update(DirectoryPojo pojo) throws ApiException {
        validateForEdit(pojo);

        DirectoryPojo existing = getCheck(pojo.getId());
        existing.setDirectoryName(pojo.getDirectoryName());
        existing.setParentId(pojo.getParentId());
        dao.update(existing);

        return existing;
    }

    private void validateForEdit(DirectoryPojo pojo) throws ApiException {
        DirectoryPojo existing = getCheck(pojo.getId());
        getCheck(pojo.getParentId());
        if (!existing.getDirectoryName().equals(pojo.getDirectoryName())) {
            DirectoryPojo sameNamePojo = dao.select("directoryName", pojo.getDirectoryName());
            checkNull(sameNamePojo, "Directory already present with same name");
        }
    }


    private void validate(DirectoryPojo pojo) throws ApiException {
        //parent id valid
        getCheck(pojo.getParentId());
        // same name anywhere
        DirectoryPojo sameNamePojo = dao.select("directoryName", pojo.getDirectoryName());
        checkNull(sameNamePojo, "Directory already present with same name");
    }

}
