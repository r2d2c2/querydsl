package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberDto {
    private String username;
    private int age;
    @QueryProjection//추가후 프로젝트 빌드
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
