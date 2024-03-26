package study.querydsl.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDto {
    private String name;//ntity는 username 이다
    private int age;

    public UserDto(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
