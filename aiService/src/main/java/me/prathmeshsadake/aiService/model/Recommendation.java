package me.prathmeshsadake.aiService.model;


import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collation = "recommendations")
@Data
@Builder
public class Recommendation {
    @Id
    private String id;
    private String userId;
    private String activityId;
    private String activityType;
    private List<String> recommendations;
    private List<String> suggestions;
    private List<String> safety;

    @CreatedDate
    private LocalDateTime createdAt;
}
