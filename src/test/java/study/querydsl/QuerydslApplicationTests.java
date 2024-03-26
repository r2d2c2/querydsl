package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Hello;
import study.querydsl.entity.Member;
import study.querydsl.entity.QHello;
import study.querydsl.repository.MemberJpaRepository;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class QuerydslApplicationTests {

    @Autowired
    MemberJpaRepository memberJpaRepository;
    @Test
    public void testMember() {
        Member member = new Member("memberA");
        //Member savedMember = memberJpaRepository.save(member);
        //Member findMember = memberJpaRepository.find(savedMember.getId());
        //assertThat(findMember.getId()).isEqualTo(member.getId());
        //assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        //assertThat(findMember).isEqualTo(member); //JPA 엔티티 동일성 보장
    }

}
