package dasturlash.uz.dto.article;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Setter
@Getter
public class ArticleFilterDTO {
    private String title;
    private String region_id;
    private String category_id;
    private LocalDateTime published_date_from;
    private LocalDateTime published_date_to;
}
