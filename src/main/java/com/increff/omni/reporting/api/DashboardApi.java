package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.DashboardDao;
import com.increff.omni.reporting.dao.FavouriteDao;
import com.increff.omni.reporting.pojo.DashboardPojo;
import com.increff.omni.reporting.pojo.FavouritePojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = ApiException.class)
public class DashboardApi extends AbstractAuditApi {

    @Autowired
    private DashboardDao dao;
    @Autowired
    private FavouriteDao favouriteDao;

    public DashboardPojo add(DashboardPojo pojo) throws ApiException {
        DashboardPojo existing = getByOrgIdName(pojo.getOrgId(), pojo.getName());
        if(Objects.nonNull(existing))
            throw new ApiException(ApiStatus.BAD_DATA, "Dashboard already exists with name: " + pojo.getName() + " for orgId: " + pojo.getOrgId());
        dao.persist(pojo);
        return pojo;
    }

    public DashboardPojo update(Integer id, DashboardPojo updated) throws ApiException {
        DashboardPojo existing = getCheck(id);
        existing.setName(updated.getName());
        return existing;
    }

    public void delete(Integer id) throws ApiException {
        DashboardPojo pojo = getCheck(id);
        deleteFavByFavId(pojo.getId());
        dao.remove(pojo);
    }

    public DashboardPojo getCheck(Integer id, Integer orgId) throws ApiException {
        DashboardPojo pojo = getCheck(id);
        if(!pojo.getOrgId().equals(orgId))
            throw new ApiException(ApiStatus.BAD_DATA, "Dashboard does not belong to orgId: " + orgId);
        return pojo;
    }

    public List<DashboardPojo> getByOrgId(Integer orgId) {
        return dao.getByOrgId(orgId);
    }
    public DashboardPojo getByOrgIdName(Integer orgId, String name) {
        return dao.getByOrgIdName(orgId, name);
    }

    public FavouritePojo getFavByOrgUser(Integer orgId, Integer userId) {
        return favouriteDao.getByOrgUser(orgId, userId);
    }

    public FavouritePojo getFavByOrg(Integer orgId) {
        return favouriteDao.getByOrgUser(orgId, null);
    }


    public FavouritePojo setFav(FavouritePojo pojo) {
        FavouritePojo existing = favouriteDao.getByOrgUser(pojo.getOrgId(), pojo.getUserId());
        if (Objects.isNull(existing)) {
            favouriteDao.persist(pojo);
            return pojo;
        } else {
            existing.setFavId(pojo.getFavId());
            existing.setOrgId(pojo.getOrgId());
            favouriteDao.update(existing);
            return existing;
        }
    }

    public void deleteFavById(Integer id) {
        FavouritePojo pojo = favouriteDao.select(id);
        if (Objects.nonNull(pojo))
            favouriteDao.remove(pojo);
    }

    public void deleteFavByFavId(Integer favId) {
        List<FavouritePojo> pojos = favouriteDao.selectMultiple("favId", favId);
        if (Objects.nonNull(pojos) && !pojos.isEmpty())
            pojos.forEach(favouriteDao::remove);
    }

    private DashboardPojo getCheck(Integer id) throws ApiException {
        DashboardPojo pojo = dao.select(id);
        checkNotNull(pojo, "Dashboard does not exist id: " + id);
        return pojo;
    }
}
