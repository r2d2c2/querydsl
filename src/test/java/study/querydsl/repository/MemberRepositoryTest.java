package study.querydsl.repository;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@Transactional
class MemberRepositoryTest {
    @Autowired
    EntityManager em;
    @Autowired
    MemberRepository memberRepository;

    @Test
    public void basicTest(){
        //given
        Member member=new Member("member1",10);
        memberRepository.save(member);


        //when
        Member findMember=memberRepository.findById(member.getId()).get();
        List<Member> result1 = memberRepository.findAll();
        List<Member> result2 = memberRepository.findByUsername("member1");
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

        List<MemberTeamDto> find1 = memberRepository.search(condition);

        assertThat(find1).extracting("username").containsExactly("member4");
    }
    @Test
    public void searchTest2(){
        Team teama=new Team("teamA");
        Team tesmb=new Team("teamB");
        em.persist(teama);
        em.persist(tesmb);

        Member member1=new Member("member1",10,teama);
        Member member2=new Member("member2",20,teama);
        Member member3=new Member("member3",30,tesmb);
        //Member member4=new Member("member4",40,tesmb);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        //em.persist(member4);

        MemberSearchCondition condition = new MemberSearchCondition();
        List<MemberTeamDto> find1 = memberRepository.search(condition);

        //페이지 추가
        PageRequest pageRequest= PageRequest.of(0,3);
        Page<MemberTeamDto> result=memberRepository.searchPageSimple(condition,pageRequest);


        assertThat(result.getSize()).isEqualTo(3);
        assertThat(result.getContent()).extracting("username").containsExactly("member1","member2","member3");
    }
    
    @Test
    public void querydslPredicateExecutorTest(){
        Team teama=new Team("teamA");
        Team tesmb=new Team("teamB");
        em.persist(teama);
        em.persist(tesmb);

        Member member1=new Member("member1",10,teama);
        Member member2=new Member("member2",20,teama);
        Member member3=new Member("member3",30,tesmb);
        Member member4=new Member("member4",40,tesmb);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);

        QMember member=QMember.member;
        Iterable<Member> members = memberRepository.findAll(member.age.between(10, 40).and(member.username.eq("member1")));
        for (Member membe : members) {
            System.out.println("membe = " + membe);
        }
    }
}