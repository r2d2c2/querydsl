package study.querydsl.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@ToString(of = {"id","username","age"})
//toString 주의 Team 추가시 무한 참조
public class Member {
    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    public Member(String username, int age) {
        this.username = username;
        this.age = age;
    }

    public Member(String username, int age, Team team) {
        this.username = username;
        this.age = age;
        if(team!=null){
            changeTeam(team);
        }
    }

    private void changeTeam(Team team) {
        this.team=team;
        team.getMembers().add(this);
    }

    public Member(String username) {
        this.username = username;
    }

}
