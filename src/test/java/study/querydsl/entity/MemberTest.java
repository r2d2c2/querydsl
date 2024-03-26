package study.querydsl.entity;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
@Commit//록백 안함
class MemberTest {
    @Autowired
    EntityManager entityManager;

    @Test
    public void testEntity() throws Exception{
        //given
       Team teama=new Team("teamA");
       Team tesmb=new Team("teamB");
       entityManager.persist(teama);
       entityManager.persist(tesmb);

       Member member1=new Member("member1",10,teama);
       Member member2=new Member("member1",20,teama);
       Member member3=new Member("member1",30,tesmb);
       Member member4=new Member("member1",40,tesmb);
       entityManager.persist(member1);
       entityManager.persist(member2);
       entityManager.persist(member3);
       entityManager.persist(member4);
       entityManager.flush();
       entityManager.clear();
        //when
        List<Member> members = entityManager.createQuery("select m from Member m", Member.class)
                .getResultList();
        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("member.getTeam() = " + member.getTeam());
        }
        //then

    }


}