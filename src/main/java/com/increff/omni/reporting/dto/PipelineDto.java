package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.PipelineApi;
import com.increff.omni.reporting.job.ScheduleReportTask;
import com.increff.omni.reporting.model.data.PipelineData;
import com.increff.omni.reporting.model.form.PipelineForm;
import com.increff.omni.reporting.pojo.PipelinePojo;
import com.increff.omni.reporting.util.FileUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

import static com.increff.omni.reporting.util.ConvertUtil.convertToPipelineData;

@Service
public class PipelineDto extends AbstractDto {

    @Autowired
    private PipelineApi api;
    @Autowired
    private ScheduleReportTask scheduleReportTask;

    public PipelineData add(PipelineForm form) throws ApiException {
        checkValid(form);
        PipelinePojo pojo = ConvertUtil.convert(form, PipelinePojo.class);
        pojo.setOrgId(getOrgId());
        pojo.setConfigs(form.getConfigs().toString());
        pojo = api.add(pojo);
        return convertToPipelineData(pojo);
    }

    public PipelineData update(Integer id, PipelineForm form) throws ApiException {
        checkValid(form);
        PipelinePojo pojo = ConvertUtil.convert(form, PipelinePojo.class);
        pojo.setConfigs(form.getConfigs().toString());
        pojo.setOrgId(getOrgId());
        pojo = api.updateWithUserOrgCheck(id, pojo);
        return convertToPipelineData(pojo);
    }

    public List<PipelineData> getPipelinesByUserOrg() throws ApiException {
        return getPipelinesByOrgId(getOrgId());
    }

    public List<PipelineData> getPipelinesByOrgId(Integer orgId) throws ApiException{
        return sortPipelines(convertToPipelineData(api.getByOrgId(orgId)));
    }

    public PipelineData getPipelineById(Integer id) throws ApiException {
        return convertToPipelineData(api.getCheckPipelineOrg(id, getOrgId()));
    }

    public void testConnection(PipelineForm form) throws ApiException {
        File file = new File("dummy_" + ZonedDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss")) + ".txt");

        try {
            FileUtil.writeDummyContentToFile(file);
            scheduleReportTask.uploadScheduleFiles(form.getType(), form.getConfigs().toString(), file, "increff-pipeline-test", file.getName());
        } catch (Exception e) {
            if (file.exists()) // If an exception occurs, delete the file if it exists
                FileUtil.delete(file);
            throw new ApiException(ApiStatus.BAD_DATA, "Error while testing connection: " + e.getMessage());
        } finally {
            FileUtil.delete(file);
        }
    }

    private List<PipelineData> sortPipelines(List<PipelineData> pipelineDataList) {
        pipelineDataList.sort(Comparator.comparing(PipelineData::getName));
        return pipelineDataList;
    }
}
