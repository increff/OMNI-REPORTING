package com.increff.omni.reporting.api;

import com.increff.omni.reporting.OmniReportingApplication;
//import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.dao.CustomReportAccessDao;
import com.increff.omni.reporting.pojo.CustomReportAccessPojo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import static com.increff.omni.reporting.helper.CustomReportAccessTestHelper.getCustomReportAccessPojo;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class CustomReportAccessApiTest extends AbstractTest {
    @Autowired
    private CustomReportAccessApi api;
    @Test
    public void testAddCustomAccess() {
        CustomReportAccessPojo pojo = getCustomReportAccessPojo(100001, 100002);
        api.addCustomReportAccessPojo(pojo);
        api.addCustomReportAccessPojo(pojo);
        List<CustomReportAccessPojo> pojoList = api.getAllByReportId(100002);
        assertEquals(1, pojoList.size());
        assertEquals(100002, pojoList.get(0).getReportId().intValue());
        assertEquals(100001, pojoList.get(0).getOrgId().intValue());
    }
    @Test
    public void testGetByOrgId() {
        CustomReportAccessPojo pojo = getCustomReportAccessPojo(100001, 100002);
        CustomReportAccessPojo pojo2 = getCustomReportAccessPojo(100001, 100003);
        CustomReportAccessPojo pojo3 = getCustomReportAccessPojo(100002, 100002);
        api.addCustomReportAccessPojo(pojo);
        api.addCustomReportAccessPojo(pojo2);
        api.addCustomReportAccessPojo(pojo3);
        List<CustomReportAccessPojo> pojoList = api.getByOrgId(100001);
        assertEquals(2, pojoList.size());
        assertEquals(100002, pojoList.get(0).getReportId().intValue());
        assertEquals(100001, pojoList.get(0).getOrgId().intValue());
        assertEquals(100003, pojoList.get(1).getReportId().intValue());
        assertEquals(100001, pojoList.get(0).getOrgId().intValue());
    }
    @Test
    public void testDeleteByReportId() {
        CustomReportAccessPojo pojo = getCustomReportAccessPojo(100001, 100002);
        CustomReportAccessPojo pojo2 = getCustomReportAccessPojo(100001, 100003);
        CustomReportAccessPojo pojo3 = getCustomReportAccessPojo(100002, 100002);
        api.addCustomReportAccessPojo(pojo);
        api.addCustomReportAccessPojo(pojo2);
        api.addCustomReportAccessPojo(pojo3);
        api.deleteByReportId(100002);
        List<CustomReportAccessPojo> pojoList = api.getAllByReportId(100003);
        assertEquals(1, pojoList.size());
        assertEquals(100003, pojoList.get(0).getReportId().intValue());
        assertEquals(100001, pojoList.get(0).getOrgId().intValue());
    }
    @Test
    public void testDeleteById() {
        CustomReportAccessPojo pojo = getCustomReportAccessPojo(100001, 100002);
        CustomReportAccessPojo pojo2 = getCustomReportAccessPojo(100001, 100003);
        CustomReportAccessPojo pojo3 = getCustomReportAccessPojo(100002, 100002);
        api.addCustomReportAccessPojo(pojo);
        api.addCustomReportAccessPojo(pojo2);
        api.addCustomReportAccessPojo(pojo3);
        api.deleteById(pojo2.getId());
        List<CustomReportAccessPojo> pojoList = api.getAllByReportId(100002);
        assertEquals(2, pojoList.size());
        assertEquals(100002, pojoList.get(0).getReportId().intValue());
        assertEquals(100001, pojoList.get(0).getOrgId().intValue());
        assertEquals(100002, pojoList.get(1).getReportId().intValue());
        assertEquals(100002, pojoList.get(1).getOrgId().intValue());
    }
    @Test
    public void testDeleteByIdWithWrongId() {
        CustomReportAccessPojo pojo = getCustomReportAccessPojo(100001, 100002);
        CustomReportAccessPojo pojo2 = getCustomReportAccessPojo(100001, 100003);
        CustomReportAccessPojo pojo3 = getCustomReportAccessPojo(100002, 100002);
        api.addCustomReportAccessPojo(pojo);
        api.addCustomReportAccessPojo(pojo2);
        api.addCustomReportAccessPojo(pojo3);
        api.deleteById(pojo2.getId() + 10);
        List<CustomReportAccessPojo> pojoList = api.getAllByReportId(100001);
        assertEquals(0, pojoList.size());
    }
}