package com.increff.omni.reporting.dao;

import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.model.constants.VisualizationType;
import com.increff.omni.reporting.pojo.ReportPojo;
import com.increff.commons.springboot.db.dao.AbstractDao;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Transactional
public class ReportDao extends AbstractDao<ReportPojo> {

    public List<ReportPojo> getByTypeAndSchema(ReportType type, Integer schemaVersionId, Boolean isChart, VisualizationType visualization) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportPojo> query = cb.createQuery(ReportPojo.class);
        Root<ReportPojo> root = query.from(ReportPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("type"), type),
                        cb.equal(root.get("schemaVersionId"), schemaVersionId),
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

    public List<ReportPojo> getByIdsAndSchema(List<Integer> ids, Integer schemaVersionId, Boolean isChart) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportPojo> query = cb.createQuery(ReportPojo.class);
        Root<ReportPojo> root = query.from(ReportPojo.class);
        query.where(
                cb.and(
                        root.get("id").in(ids),
                        cb.equal(root.get("schemaVersionId"), schemaVersionId),
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

    public ReportPojo getByAliasAndSchema(String alias, Integer schemaVersionId, Boolean isChart) {
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportPojo> query = cb.createQuery(ReportPojo.class);
        Root<ReportPojo> root = query.from(ReportPojo.class);
        query.where(
                cb.and(
                        cb.equal(root.get("alias"), alias),
                        cb.equal(root.get("schemaVersionId"), schemaVersionId),
                        cb.equal(root.get("isChart"), isChart)
                )
        );
        TypedQuery<ReportPojo> tQuery = createQuery(query);
        return selectSingleOrNull(tQuery);
    }

    public List<ReportPojo> getByAliasAndSchema(List<String> aliasList, Integer schemaVersionId, Boolean isChart) {
        if(aliasList.isEmpty())
            return new ArrayList<>();
        CriteriaBuilder cb = this.em.getCriteriaBuilder();
        CriteriaQuery<ReportPojo> query = cb.createQuery(ReportPojo.class);
        Root<ReportPojo> root = query.from(ReportPojo.class);
        query.where(
                cb.and(
                        root.get("alias").in(aliasList),
                        cb.equal(root.get("schemaVersionId"), schemaVersionId),
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
}
