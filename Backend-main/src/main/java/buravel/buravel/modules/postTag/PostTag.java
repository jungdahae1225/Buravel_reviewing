package buravel.buravel.modules.postTag;

import buravel.buravel.modules.post.Post;
import buravel.buravel.modules.tag.Tag;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.*;

import javax.persistence.*;

@NamedEntityGraph(name = "PostTag.withTag",attributeNodes = {
        @NamedAttributeNode("tag")
})
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class PostTag {
    @Id
    @GeneratedValue
    @Column(name = "postTag_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    private Tag tag;
}
