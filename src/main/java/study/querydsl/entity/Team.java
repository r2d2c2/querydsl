package study.querydsl.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity@Getter@Setter
@NoArgsConstructor//protected 기본 생성자 선언
@ToString(of = {"id","name"})
public class Team {
    @Id@GeneratedValue
    private Long id;
    private String name;
    @OneToMany(mappedBy = "team")
    private List<Member> members=new ArrayList<>();

    public Team(String name){
        this.name=name;
    }
}
