package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.model.constants.AppName;
import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.constants.VisualizationType;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.nextscm.commons.spring.db.AbstractDao;
import lombok.extern.log4j.Log4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.*;
import java.util.stream.Collectors;

@Log4j
@Repository
@Transactional
public class ReportDao extends AbstractDao<ReportPojo> {

    private static final String SELECT_REPORT_BY_ID_AND_MULTIPLE_APP_NAMES = "select r from ReportPojo r join SchemaVersionPojo s on r.schemaVersionId = s.id where s.appName in :appNames and r.id = :reportId";

    public List<ReportPojo> getByTypeAndSchema(ReportType type, List<Integer> schemaVersionIds, Boolean isChart, VisualizationType visualization) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportPojo> query = cb.createQuery(ReportPojo.class);
        Root<ReportPojo> root = query.from(ReportPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("type"), type),
                        cb.in(root.get("schemaVersionId")).value(schemaVersionIds),
                        cb.equal(root.get("isEnabled"), true),
                        cb.equal(root.get("isChart"), isChart),
                        root.get("chartType").in(parseVisualization(visualization))
                )
        );
        TypedQuery<ReportPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }

    public ReportPojo getByNameAndSchema(String name, Integer schemaVersionId, Boolean isChart) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportPojo> query = cb.createQuery(ReportPojo.class);
        Root<ReportPojo> root = query.from(ReportPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("name"), name),
                        cb.equal(root.get("schemaVersionId"), schemaVersionId),
                        cb.equal(root.get("isChart"), isChart)
                )
        );
        TypedQuery<ReportPojo> tQuery = createQuery(query);
        return selectSingleOrNull(tQuery);
    }

    public List<ReportPojo> getByIdsAndSchema(List<Integer> ids, List<Integer> schemaVersionIds, Boolean isChart) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportPojo> query = cb.createQuery(ReportPojo.class);
        Root<ReportPojo> root = query.from(ReportPojo.class);
        query.where(
                cb.and(
                        root.get("id").in(ids),
                        cb.in(root.get("schemaVersionId")).value(schemaVersionIds),
                        cb.equal(root.get("isEnabled"), true),
                        cb.equal(root.get("isChart"), isChart)
                )
        );
        TypedQuery<ReportPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }

    public List<ReportPojo> getByIds(List<Integer> ids, Boolean isChart) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportPojo> query = cb.createQuery(ReportPojo.class);
        Root<ReportPojo> root = query.from(ReportPojo.class);
        query.where(
                cb.and(
                        root.get("id").in(ids),
                        cb.equal(root.get("isChart"), isChart)
                )
        );
        TypedQuery<ReportPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }

    public List<ReportPojo> getByIds(List<Integer> ids) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportPojo> query = cb.createQuery(ReportPojo.class);
        Root<ReportPojo> root = query.from(ReportPojo.class);
        query.where(
                root.get("id").in(ids)
        );
        TypedQuery<ReportPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }


        public ReportPojo getByAliasAndSchema(String alias, List<Integer> schemaVersionId, Boolean isChart) {
        if(schemaVersionId.isEmpty())
            return null;
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportPojo> query = cb.createQuery(ReportPojo.class);
        Root<ReportPojo> root = query.from(ReportPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("alias"), alias),
                        cb.in(root.get("schemaVersionId")).value(schemaVersionId),
                        cb.equal(root.get("isChart"), isChart)
                )
        );
        TypedQuery<ReportPojo> tQuery = createQuery(query);
        return selectSingleOrNull(tQuery);
    }

    public List<ReportPojo> getByAliasAndSchema(List<String> aliasList, List<Integer> schemaVersionIds, Boolean isChart) {
        if(aliasList.isEmpty() || schemaVersionIds.isEmpty())
            return new ArrayList<>();
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportPojo> query = cb.createQuery(ReportPojo.class);
        Root<ReportPojo> root = query.from(ReportPojo.class);
        query.where(
                cb.and(
                        root.get("alias").in(aliasList),
                        cb.in(root.get("schemaVersionId")).value(schemaVersionIds),
                        cb.equal(root.get("isChart"), isChart)
                )
        );
        TypedQuery<ReportPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }

    public List<ReportPojo> getBySchemaVersionAndTypes(Integer schemaVersionId, VisualizationType visualization) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportPojo> query = cb.createQuery(ReportPojo.class);
        Root<ReportPojo> root = query.from(ReportPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("schemaVersionId"), schemaVersionId),
                        root.get("chartType").in(parseVisualization(visualization))
                )
        );
        TypedQuery<ReportPojo> tQuery = createQuery(query);
        return selectMultiple(tQuery);
    }

    private List<ChartType> parseVisualization(VisualizationType visualization){
        if(Objects.isNull(visualization) || visualization.equals(VisualizationType.ALL))
            return Arrays.stream(ChartType.values()).collect(Collectors.toList());
        if(visualization.equals(VisualizationType.REPORTS))
            return Collections.singletonList(ChartType.REPORT);
        if(visualization.equals(VisualizationType.CHARTS))
            return Arrays.stream(ChartType.values()).filter(chartType -> chartType != ChartType.REPORT)
                .collect(Collectors.toList());

        return Collections.singletonList(ChartType.valueOf(visualization.name()));
    }

    public ReportPojo getByIdAndAppNameIn(Integer reportId, Set<AppName> appNames) {
        log.debug("Fetching report by id : " + reportId + " and appNames : " + appNames);
        if(appNames.isEmpty()) return null;

        TypedQuery<ReportPojo> query = em.createQuery(SELECT_REPORT_BY_ID_AND_MULTIPLE_APP_NAMES, ReportPojo.class);
        query.setParameter("appNames", appNames);
        query.setParameter("reportId", reportId);
        return selectSingleOrNull(query);
    }
}
