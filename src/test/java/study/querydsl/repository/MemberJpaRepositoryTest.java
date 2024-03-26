package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {
    @Autowired
    EntityManager em;
    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test//순수 jpq
    public void basicTest(){
        //given
        Member member=new Member("member1",10);
        memberJpaRepository.save(member);


        //when
        Member findMember=memberJpaRepository.findById(member.getId()).get();
        List<Member> result1 = memberJpaRepository.findAll();
        List<Member> result2 = memberJpaRepository.findByUsername("member1");
        //then
        assertThat(member).isEqualTo(findMember);
        assertThat(result1).isEqualTo(List.of(member));
        //객체 member를 list로 감싸서 비교
        assertThat(result2).containsExactly(member);
    }
    @Test//Querydsl 변경
    public void basicQuerydslTest(){
        //given
        Member member=new Member("member1",10);
        memberJpaRepository.save(member);


        //when
        Member findMember=memberJpaRepository.findById(member.getId()).get();
        List<Member> result1 = memberJpaRepository.findAll_Querydsl();
        List<Member> result2 = memberJpaRepository.findByUsername_Querydsl("member1");

        //then
        assertThat(member).isEqualTo(findMember);
        assertThat(result1).isEqualTo(List.of(member));
        //객체 member를 list로 감싸서 비교
        assertThat(result2).containsExactly(member);
    }

    @Test
    public void searchTest(){
        Team teama=new Team("teamA");
        Team tesmb=new Team("teamB");
        em.persist(teama);
        em.persist(tesmb);

        Member member1=new Member("Member1",10,teama);
        Member member2=new Member("member2",20,teama);
        Member member3=new Member("Member3",30,tesmb);
        Member member4=new Member("member4",40,tesmb);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        condition.setAgeGoe(35);
        condition.setAgeLoe(40);
        condition.setTeamName("teamB");
        //BooleanBuilder 빙식
        //List<MemberTeamDto> find1 = memberJpaRepository.searchByBuilder(condition);
        //Where 방식
        List<MemberTeamDto> find1 = memberJpaRepository.search(condition);
        List<Member> result3 = memberJpaRepository.findMember(condition);
        assertThat(find1).extracting("username").containsExactly("member4");
        assertThat(result3).extracting("username").containsExactly("member4");

    }
}