package me.prathmeshsadake.aiService.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.prathmeshsadake.aiService.model.Recommendation;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Cleans duplicate recommendations created by the RabbitMQ + on-demand generation race.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RecommendationCleanup implements ApplicationRunner {

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(ApplicationArguments args) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.group("activityId").count().as("count").push("$_id").as("ids"),
                Aggregation.match(Criteria.where("count").gt(1))
        );

        List<Map> duplicates = mongoTemplate.aggregate(
                aggregation, "recommendations", Map.class).getMappedResults();

        int removed = 0;
        for (Map duplicate : duplicates) {
            String activityId = String.valueOf(duplicate.get("_id"));
            List<Recommendation> docs = mongoTemplate.find(
                    Query.query(Criteria.where("activityId").is(activityId))
                            .with(Sort.by(Sort.Direction.DESC, "createdAt")),
                    Recommendation.class
            );
            if (docs.size() <= 1) {
                continue;
            }
            List<String> dropIds = docs.stream().skip(1).map(Recommendation::getId).toList();
            removed += mongoTemplate.remove(
                    Query.query(Criteria.where("_id").in(dropIds)), Recommendation.class).getDeletedCount();
            log.info("Removed {} duplicate recommendation(s) for activityId={}", dropIds.size(), activityId);
        }

        if (removed > 0) {
            log.info("Recommendation cleanup removed {} duplicate document(s)", removed);
        }
    }
}
