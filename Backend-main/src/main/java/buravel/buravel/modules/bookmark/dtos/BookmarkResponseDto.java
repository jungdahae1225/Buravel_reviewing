package buravel.buravel.modules.bookmark.dtos;

import buravel.buravel.modules.account.dtos.AccountResponseDto;
import buravel.buravel.modules.bookmarkPost.dtos.BookmarkPostResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkResponseDto {
    private Long id;
    private String bookmarkTitle;
    private AccountResponseDto accountResponseDto;
    private List<BookmarkPostResponseDto> bookmarkPostResponseDtos = new ArrayList<>();
    private List<String> bookmarkImages = new ArrayList<>();
}
