package buravel.buravel.modules.plan.dtos;

import buravel.buravel.modules.post.dtos.PostDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Lob;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlanDto {

    @NotBlank
    private String planTitle;
    @Lob
    private String planImage;
    @NotNull
    private boolean published;
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;
    @NotNull
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;
    private String planTag;

    private PostDto[][] postDtos;
}
