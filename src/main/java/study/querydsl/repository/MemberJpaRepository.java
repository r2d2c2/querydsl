package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;

import java.util.List;
import java.util.Optional;

import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory jpaQueryFactory;

    public MemberJpaRepository(EntityManager em) {
        this.em = em;
        this.jpaQueryFactory=new JPAQueryFactory(em);
    }

    public void save(Member member) {
        em.persist(member);
        //return member;
    }
    public Member find(Long id) {
        return em.find(Member.class, id);
    }
    public Optional<Member> findById(Long id){
        Member findMember=em.find(Member.class,id);
        return Optional.ofNullable(findMember);
    }
    public List<Member> findAll(){
        return em.createQuery("select m from Member m",Member.class)
                .getResultList();
    }
    public List<Member> findAll_Querydsl(){
        return jpaQueryFactory
                .selectFrom(member)
                .fetch();
    }
    public List<Member> findByUsername(String username){
        return em.createQuery("select m from Member m where m.username=:username"
        ,Member.class).setParameter("username",username)
                .getResultList();
    }
    public List<Member> findByUsername_Querydsl(String username){
        return  jpaQueryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    //dto (Builder)
    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition){
        BooleanBuilder builder=new BooleanBuilder();
        //spring StringUtils 사용 (null,""제외)
        if(StringUtils.hasText(condition.getUsername())){
            builder.and(member.username.eq(condition.getUsername()));
        }
        if(StringUtils.hasText(condition.getTeamName())){
            builder.and(team.name.eq(condition.getTeamName()));
        }
        if(condition.getAgeGoe()!=null){
            builder.and(member.age.goe(condition.getAgeGoe()));
        }
        if(condition.getAgeLoe()!=null){
            builder.and(member.age.loe(condition.getAgeLoe()));
        }
        return jpaQueryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(builder)
                .fetch();
    }
    // Where 사용
    public List<MemberTeamDto> search(MemberSearchCondition condition){
        return jpaQueryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername())
                ,teamNameEq(condition.getTeamName())
                ,ageGoe(condition.getAgeGoe())
                ,ageLoe(condition.getAgeLoe()))
                .fetch();

    }

    private BooleanExpression usernameEq(String username) {
        //없으면 null
        return StringUtils.hasText(username) ? member.username.eq(username) :null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return StringUtils.hasText(teamName)?team.name.eq(teamName):null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe!=null ? member.age.goe(ageGoe):null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe!=null?member.age.loe(ageLoe):null;
    }

    //where 파라미터 방식은 이런식으로 재사용이 가능하다.
    public List<Member> findMember(MemberSearchCondition condition) {
        return jpaQueryFactory
                .selectFrom(member)
                .leftJoin(member.team, team)
                .where(usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetch();
    }
}
