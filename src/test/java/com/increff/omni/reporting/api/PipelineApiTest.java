package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.model.constants.PipelineType;
import com.increff.omni.reporting.pojo.PipelinePojo;
import com.increff.commons.springboot.common.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.increff.omni.reporting.helper.PipelineTestHelper.*;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PipelineApiTest extends AbstractTest {

    @Autowired
    private PipelineApi pipelineApi;

    @Test
    public void testAddPipeline() throws ApiException {
        PipelinePojo pojo = getPipelinePojo("Pipeline 1", PipelineType.GCP,
                getGCPConfigString("bucket.com", "testBucket", "abc"), orgId);
        pipelineApi.add(pojo);

        PipelinePojo existing = pipelineApi.getCheck(pojo.getId());
        assertEquals(pojo, existing);
    }

    @Test
    public void testAddAwsPipeline() throws ApiException {
        PipelinePojo pojo = getPipelinePojo("Pipeline 1", PipelineType.AWS,
                getAWSConfigString("bucket.com", "testBucket", "us-east-1",
                        "testAccess", "testSecret"), orgId);
        pipelineApi.add(pojo);

        PipelinePojo existing = pipelineApi.getCheck(pojo.getId());
        assertEquals(pojo, existing);
    }

    @Test
    public void testGetByOrgId() throws ApiException {
        PipelinePojo pojo = getPipelinePojo("Pipeline 1", PipelineType.GCP,
                getGCPConfigString("bucket.com", "testBucket", "abc"), orgId);
        PipelinePojo pojo2 = getPipelinePojo("Pipeline 2", PipelineType.GCP,
                getGCPConfigString("bucket.com", "testBucket", "abc"), orgId);
        pipelineApi.add(pojo);
        pipelineApi.add(pojo2);
        Integer otherOrgId = 2;
        pipelineApi.add(getPipelinePojo("Other Org Pipeline", PipelineType.GCP,
                getGCPConfigString("bucket.com", "testBucket", "abc"), otherOrgId));

        List<PipelinePojo> existing = pipelineApi.getByOrgId(orgId);
        assertEquals(2, existing.size());
        existing = pipelineApi.getByOrgId(otherOrgId);
        assertEquals(1, existing.size());
        assertEquals("Other Org Pipeline", existing.get(0).getName());
    }

    @Test
    public void testGetCheckPipelineOrg() throws ApiException {
        PipelinePojo pojo = getPipelinePojo("Pipeline 1", PipelineType.GCP,
                getGCPConfigString("bucket.com", "testBucket", "abc"), orgId);
        pipelineApi.add(pojo);
        PipelinePojo existing = pipelineApi.getCheckPipelineOrg(pojo.getId(), orgId);
        assertEquals(pojo, existing);

    }

    @Test
    public void testGetCheckPipelineWithWrongOrg() throws ApiException {
        PipelinePojo pojo = getPipelinePojo("Pipeline 1", PipelineType.GCP,
                getGCPConfigString("bucket.com", "testBucket", "abc"), orgId);
        pipelineApi.add(pojo);
        try {
            pipelineApi.getCheckPipelineOrg(pojo.getId(), 2);
        } catch (ApiException e) {
//            assertThat(e.getMessage(), containsString("does not belong"));
        }
    }

    @Test
    public void testUpdatePipeline() throws ApiException {
        PipelinePojo pojo = getPipelinePojo("Pipeline 1", PipelineType.GCP,
                getGCPConfigString("bucket.com", "testBucket", "abc"), orgId);
        pipelineApi.add(pojo);
        pojo.setName("Pipeline 2");
        pojo.setConfigs(getGCPConfigString("bucket.com", "testBucket", "abc_updated"));
        pojo.setType(PipelineType.AWS);
        pipelineApi.updateWithUserOrgCheck(pojo.getId(), pojo);
        PipelinePojo existing = pipelineApi.getCheck(pojo.getId());
        assertEquals(pojo, existing);

    }

    @Test
    public void testUpdatePipelineWithWrongOrg() throws ApiException {
        PipelinePojo pojo = getPipelinePojo("Pipeline 1", PipelineType.GCP,
                getGCPConfigString("bucket.com", "testBucket", "abc"), orgId);
        pipelineApi.add(pojo);
        pojo.setName("Pipeline 2");
        try {
            pipelineApi.updateWithUserOrgCheck(pojo.getId(), getPipelinePojo("Pipeline 2", PipelineType.GCP,
                    getGCPConfigString("bucket.com", "testBucket", "abc"), 2));
        } catch (ApiException e) {
//            assertThat(e.getMessage(), containsString("does not belong"));
        }
    }


}
