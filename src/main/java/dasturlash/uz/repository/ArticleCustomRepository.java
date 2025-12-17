package dasturlash.uz.repository;

import dasturlash.uz.dto.FilterResultDTO;
import dasturlash.uz.dto.article.ArticleFilterDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ArticleCustomRepository {
    @Autowired
    private EntityManager entityManager;

    public FilterResultDTO<Object[]> filter(ArticleFilterDTO filter, int page, int size) {

        StringBuilder condition = new StringBuilder(" where a.visible = true");
        Map<String,Object> params = new HashMap<>();
        if(filter.getTitle() != null && !filter.getTitle().isEmpty()){
            condition.append(" and a.title like :title");
            params.put("title", "%"+filter.getTitle()+"%");
        }
        if(filter.getRegion_id() != null && !filter.getRegion_id().isEmpty()){
            condition.append(" and a.region_id like :region_id");
            params.put("region_id", "%"+filter.getRegion_id()+"%");
        }
        if(filter.getCategory_id() != null && !filter.getCategory_id().isEmpty()){
            condition.append(" and a.category_id like :category_id");
            params.put("category_id", "%"+filter.getCategory_id()+"%");
        }
        if(filter.getPublished_date_from() != null & filter.getPublished_date_to() != null){
            condition.append(" and a.published_date between :fromDate  and :toDate ");
            params.put("fromDate", LocalDateTime.of(LocalDate.from(filter.getPublished_date_from()), LocalTime.MIN));
            params.put("toDate", LocalDateTime.of(LocalDate.from(filter.getPublished_date_to()), LocalTime.MAX));
        }else if (filter.getPublished_date_from() != null) { // 01.01.2021
            condition.append(" and a.published_date >= :fromDate");
            params.put("fromDate", LocalDateTime.of(LocalDate.from(filter.getPublished_date_from()), LocalTime.MIN));
        } else if (filter.getPublished_date_to() != null) { // 01.01.2024
            condition.append(" and a.published_date <= :toDate ");
            params.put("toDate", LocalDateTime.of(LocalDate.from(filter.getPublished_date_to()), LocalTime.MAX));
        }

        StringBuilder selectBuilder = new StringBuilder(" Select a.title as title, a.region_id as region_id, a.category_id as category_id "+
                " a.published_date as published_date, a.publised_date as published_date "+
                " from article a");
        selectBuilder.append("left join article_tag as at on a.article_id = a.article_id ");
        StringBuilder countBuilder = new StringBuilder("Select count(*) From article a");
        selectBuilder.append(condition);
        countBuilder.append(condition);
        selectBuilder.append(" GROUP BY a.title, a.region_id, a.category_id, a.published_date_from, a.published_date_to ");
        countBuilder.append(" GROUP BY a.title, a.region_id, a.category_id, a.published_date_from, a.published_date_to");
        //
        Query selectQuery = entityManager.createNativeQuery(selectBuilder.toString());
        Query countQuery = entityManager.createNativeQuery(countBuilder.toString());

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            selectQuery.setParameter(entry.getKey(), entry.getValue());
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }

        selectQuery.setMaxResults(size); // limit
        selectQuery.setFirstResult(page * size); // offset

        List<Object[]> articleList = selectQuery.getResultList();
        Long totalCount = (Long) countQuery.getSingleResult();

        return new FilterResultDTO<>(articleList, totalCount);

    }
}
