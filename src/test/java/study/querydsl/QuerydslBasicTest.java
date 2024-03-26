package study.querydsl;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

import java.util.List;

import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {
    @Autowired
    EntityManager entityManager;

    JPAQueryFactory queryFactory;

    @BeforeEach//개별 태스트
    public void before(){
        queryFactory=new JPAQueryFactory(entityManager);

        Team teama=new Team("teamA");
        Team tesmb=new Team("teamB");
        entityManager.persist(teama);
        entityManager.persist(tesmb);

        Member member1=new Member("Member1",10,teama);
        Member member2=new Member("member2",20,teama);
        Member member3=new Member("Member3",30,tesmb);
        Member member4=new Member("member4",40,tesmb);
        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.persist(member3);
        entityManager.persist(member4);

    }
    @Test
    public void startJPQL(){
        //맴버1 찾기
        Member member1 = entityManager.createQuery("select m from Member m where m.username= : username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();
        System.out.println(member1.getUsername());
        Assertions.assertThat(member1.getUsername()).isEqualTo("member1");
        assertThat(member1.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl(){
        QMember m = new QMember("name");

        Member fetchOne = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))
                //username 에 member1과 같은것을 호출
                .fetchOne();
        assertThat(fetchOne.getUsername()).isEqualTo("member1");
    }
    @Test
    public void startQuerydsl2(){
        QMember m = member;

        Member fetchOne = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1"))
                //username 에 member1과 같은것을 호출
                .fetchOne();
        assertThat(fetchOne.getUsername()).isEqualTo("member1");
    }
    @Test
    public void startQuerydsl3(){//권장
        Member fetchOne = queryFactory
                .select(member) //static 임폴트하여 QMamber.member 간소화
                .from(member)
                .where(member.username.eq("member1"))
                //username 에 member1과 같은것을 호출
                .fetchOne();
        assertThat(fetchOne.getUsername()).isEqualTo("member1");
    }
    @Test
    public void search() throws Exception{
        Member find1 = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1").and(member.age.eq(10)))
                .fetchOne();
        assertThat(find1.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch() throws Exception{
        //given
        List<Member> fetch=queryFactory
                .selectFrom(member)
                .fetch();//리스트로 저장
        Member fetchOne=queryFactory
                .selectFrom(member)
                .fetchOne();//하나만 저장
        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();//limit(1) 추가


        QueryResults<Member> memberQueryResults = queryFactory
                .selectFrom(member)
                .fetchResults();//레거시
        long total = memberQueryResults.getTotal();
        //카운트 값 저장
        List<Member> cont=memberQueryResults.getResults();
        //맴버의 모든 값을 가지고 오기
        long count = queryFactory
                .selectFrom(member)
                .fetchCount();//레거시
        //현제 버전에서는 지원을 하지만 향후에는 그냥 fethc로 가지고 직접 count 값을 가지고 와야 한다
    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 올림차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() throws Exception{
        entityManager.persist(new Member(null,100));
        entityManager.persist(new Member("member5",100));
        entityManager.persist(new Member("member6",100));

        List<Member> fetch = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast())//nullsFirst()도 있다
                .fetch();//이름 값은 동일 하여 이름과 ,null 값으로 순서가 정해진다
        Member member5=fetch.get(0);
        Member member6=fetch.get(1);
        Member membernull=fetch.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(membernull.getUsername()).isNull();
    }
    @Test
    public void paging1(){
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)//시작 위치 1 (0부터 시작한다)
                .limit(2)//2개만
                .fetch();//오른차순으로 2개 저장
        System.out.println("fetch = " + fetch);
        //fetch = [Member(id=4, username=member1, age=40), Member(id=1, username=member1, age=10)]
        assertThat(fetch.size()).isEqualTo(2);
    }
    @Test
    public void aggregation(){
        //given
        List<Tuple> fetch = queryFactory
                .select(//집합 구하기
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                ).from(member)
                .fetch();
        for (Tuple tuple : fetch) {
            System.out.println("tuple = " + tuple);
        }
        Tuple tuple=fetch.get(0);
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 편균 구하기
     */
    @Test
    public void group(){
        List<Tuple> fetch = queryFactory
                .select(team.name, member.age.avg())//이름과 나이 검색
                .from(member)
                .join(member.team, team)//테이블 조인
                .groupBy(team.name)//team의 이름으로 그룹 생성
                .fetch();
        Tuple teamA = fetch.get(0);
        Tuple teamB = fetch.get(1);
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);
    }
    @Test//team에 있는 모든 member를 호출
    public void join() throws Exception{
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .join(member.team, team)
                .where(team.name.eq("teamA"))//teamA의 모든 멤버 구하기
                .fetch();
        assertThat(fetch)
                .extracting("username")
                .containsExactly("member1","member2");
    }

    @Test
    public void theta_join(){//회원 이름이 팀이름인 회원 조회
        entityManager.persist(new Member("teamA"));
        entityManager.persist(new Member("teamB"));
        entityManager.persist(new Member("teamC"));
        List<Member> fetch = queryFactory
                .select(member)
                .from(member, team)
                .where(member.username.eq(team.name))
                .fetch();
        assertThat(fetch)
                .extracting("username")
                .containsExactly("teamA","teamB");
    }
    @Test
    public void join_on_filtering(){

        List<Tuple> fetch = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team)
                .on(team.name.eq("teamA"))
                .fetch();//teamA 기준으로 모든 멤버 가지고 오기
        for (Tuple tuple : fetch) {
            System.out.println("tuple = " + tuple);
        }
        /*출력
        tuple = [Member(id=1, username=member1, age=10), Team(id=1, name=teamA)]
        tuple = [Member(id=2, username=member2, age=20), Team(id=1, name=teamA)]
        tuple = [Member(id=3, username=member3, age=30), null]
        tuple = [Member(id=4, username=member4, age=40), null]*/
    }
    @Test
    public void join_on_no_relation(){//회원 이름이 팀이름인 회원 조회
        entityManager.persist(new Member("teamA"));
        entityManager.persist(new Member("teamB"));
        entityManager.persist(new Member("teamC"));
        List<Tuple> fetch = queryFactory
                .select(member, team)
                .from(member)
                .join(team)
                .on(member.username.eq(team.name))
                .fetch();
        for (Tuple tuple : fetch) {
            System.out.println("tuple = " + tuple);
        }
    }

    @PersistenceUnit
    EntityManagerFactory emf;//외부 테이블 진짜 호출 됬는지 확인
    @Test
    public void fetchJoin() throws Exception{
        entityManager.flush();
        entityManager.clear();
        Member member1 = queryFactory
                .selectFrom(member)
                .join(member.team,team).fetchJoin()//조금이라도 연관된거는 다 호출
                .where(member.username.eq("member1"))
                .fetchOne();
        //로딩이 된거인지 확인
        boolean loaded=emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());
        assertThat(loaded).as("페치 조인 적용").isTrue();//에러가 나면 as 출력
    }@Test
    public void fetchJoinNo() throws Exception{
        entityManager.flush();
        entityManager.clear();
        Member member1 = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        //로딩이 된거인지 확인
        boolean loaded=emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());
        assertThat(loaded).as("페치 조인 미적용").isFalse();//에러가 나면 as 출력
    }

    @Test
    public void subQuery(){
        //나이가 가장 많은 회원 조회
        QMember memberSub=new QMember("memberSub");
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();
        assertThat(fetch).extracting("age").containsExactly(40);
    }

    @Test
    public void subQuerygoe(){
        //나이가 편균 이상인 회원 조회
        QMember memberSub=new QMember("memberSub");
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .where(member.age.goe(
                        select(memberSub.age.avg())
                                .from(memberSub)
                ))
                .fetch();
        assertThat(fetch).extracting("age").containsExactly(30,40);
    }
    @Test
    public void subQueryIn(){
        //나이가 10살 초괴하는 회원 조회
        QMember memberSub=new QMember("memberSub");
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .where(member.age.in(
                        select(memberSub.age)
                                .from(memberSub)
                                .where(memberSub.age.gt(10))
                ))
                .fetch();
        assertThat(fetch).extracting("age").containsExactly(20,30,40);
    }
    @Test
    public void selectSubQuery(){
        QMember memberSub=new QMember("memberSub");
        List<Tuple> fetch = queryFactory
                .select(member.username,
                        select(memberSub.age.avg())
                                .from(memberSub)
                ).from(member)
                .fetch();
        for (Tuple tuple : fetch) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void basicCase(){
        List<String> fetch = queryFactory
                .select(member.age
                        .when(10).then("열살")
                        .when(20).then("스무살")
                        .otherwise("기타"))
                .from(member)
                .fetch();
        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }
    @Test
    public void complexCase(){
        List<String> fetch = queryFactory
                .select(new CaseBuilder()
                        .when(member.age.between(0, 20)).then("0~20살")
                        .when(member.age.between(21, 30)).then("21~30살")
                        .otherwise("기타")
                ).from(member)
                .fetch();
        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void constant(){//상수 A가 출력되는데 sql에서는 명령어가 아에 없다(오직 자바)
        List<Tuple> fetch = queryFactory
                .select(member.username, Expressions.constant("A"))
                .from(member)
                .fetch();
        for (Tuple tuple : fetch) {
            System.out.println("tuple = " + tuple);
        }
    }
    @Test
    public void concat(){// 문자와 숫자 합치기 이름_나이
        List<String> fetch = queryFactory
                .select(member.username.concat("_").concat(member.age.stringValue()))
                .from(member)//타입이 다르면 stringValue를 사용하여 같도록 해야한다
                .where(member.username.eq("member1"))
                .fetch();
        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void simpleProjection(){
        List<String> fetch = queryFactory
                .select(member.username)
                .from(member)
                .fetch();//반환 값이 단순 String 이면 String으로 끝
        for (String s : fetch) {
            System.out.println("s = " + s);
        }
    }
    @Test
    public void tupleProjection(){
        List<Tuple> fetch = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();//그러나 반환값이 여러 타입이면 Tuple로 기본 저장 된다
        for (Tuple tuple : fetch) {
            System.out.println("tuple.get(member.username) = " + tuple.get(member.username));
            //String 타입으로 저장 가능
            System.out.println("tuple.get(member.age) = " + tuple.get(member.age));
            //int 타입
        }
    }

    //dto 저장 방법
    @Test
    public void findDtoByJPQL(){//순수 jpql 사용법 (페키지명이 querydsl이지 querydsl 필요 없다)
        List<MemberDto> resultList = entityManager.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) " +
                        "from Member m", MemberDto.class)
                .getResultList();
        for (MemberDto memberDto : resultList) {
            System.out.println("memberDto = " + memberDto);
        }
    }
    @Test//dto의 get, set 사용
    public void findDtoBySetter(){//Querydsl 방식들
        List<MemberDto> fetch = queryFactory
                .select(Projections.bean(MemberDto.class
                        , member.username, member.age))
                .from(member)
                .fetch();
        for (var fetch1 : fetch) {
            System.out.println("fetch1 = " + fetch1);
        }
    }
    @Test//dto get,set 없이 바로 필드 호출
    public void findDtoByFileld(){
        List<MemberDto> fetch = queryFactory
                .select(Projections.fields(MemberDto.class
                        , member.username, member.age))
                .from(member)
                .fetch();
        for (var fetch1 : fetch) {
            System.out.println("fetch1 = " + fetch1);
        }
    }
    @Test//생성자를 사용하는 방식
    public void findDtoByConstructor(){
        List<MemberDto> fetch = queryFactory
                .select(Projections.constructor(MemberDto.class
                        , member.username, member.age))
                .from(member)
                .fetch();
        for (var fetch1 : fetch) {
            System.out.println("fetch1 = " + fetch1);
        }
    }
    @Test//dto의 서브 쿼리 방식
    public void findUserDto(){
        QMember memberSub = new QMember("memberSub");
        List<UserDto> fetch = queryFactory
                .select(Projections.fields(UserDto.class
                        , member.username.as("name")
                        , ExpressionUtils.as(JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub),"age")))
                .from(member)
                .fetch();
        for (var fetch1 : fetch) {
            System.out.println("fetch1 = " + fetch1);
        }
    }
    @Test//생성자를 사용하는 방식
    public void findUserDtoByConstructor(){
        List<UserDto> fetch = queryFactory
                .select(Projections.constructor(UserDto.class
                        , member.username, member.age))
                .from(member)
                .fetch();
        for (var fetch1 : fetch) {
            System.out.println("fetch1 = " + fetch1);
        }
    }
    @Test
    public void findDtoByQueryProjection(){
        List<MemberDto> fetch = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();
        for (MemberDto memberDto : fetch) {
            System.out.println("memberDto = " + memberDto);
        }
    }
    @Test
    public void dynamicQuery_BooleanBuilder(){
        String usernameParam="member1";
        Integer ageParam=null;
        
        List<Member> result=searchMember1(usernameParam,ageParam);
        assertThat(result.size()).isEqualTo(1);
    }
    private List<Member> searchMember1(String usernameParam, Integer ageParam) {
        //검색조건 null이면 넘기고 있으면 매게값과 엔티티값을 비교 하여 리턴
        BooleanBuilder builder=new BooleanBuilder();
        if(usernameParam!=null){//이름이 null 아니면
            builder.and(member.username.eq(usernameParam));
        }//null 값이 아니면 builder 에 저장된다
        if(ageParam!=null){
            builder.and(member.age.eq(ageParam));
        }
        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }
    @Test
    public void dynamicQuery_WhereParam(){
        String usernameParam="member1";
        Integer ageParam=null;

        List<Member> result=searchMember2(usernameParam,ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember2(String usernameParam, Integer ageParam) {
        return  queryFactory
                .selectFrom(member)
                .where(usernameEq(usernameParam),ageEq(ageParam))
                //where에는 null값이 삽입되면 무시된다
                .fetch();
    }

    private Predicate ageEq(Integer ageParam) {
        if(ageParam==null){
            return null;
        }
        return member.age.eq(ageParam);
    }

    private Predicate usernameEq(String usernameParam) {
        if(usernameParam==null){
            return null;
        }
        return member.username.eq(usernameParam);
    }
    @Test
    @Commit
    public void bulkUpdate(){
        //회원의 나이가 28미만이면 비회원으로 변경
        long execute = queryFactory
                .update(member)
                .set(member.username,"비회원")
                .where(member.age.lt(28))
                .execute();
        entityManager.flush();//업데이트는 바로 방여되지 않아 거의 필수
        entityManager.clear();
    }
    @Test
    @Commit
    public void bulkAdd(){//1더하기
        long execute = queryFactory
                //반환 값은 있어도 되고 없어도 된다 단 에러 확인시 필요
                .update(member)
                .set(member.age, member.age.add(1))
                .execute();
    }
    @Test
    @Commit
    public void bulkMultiply(){//2 곱하기
        long e= queryFactory
                .update(member)
                .set(member.age, member.age.multiply(2))
                .execute();
    }
    @Test
    @Commit
    public void bulkDelete(){//18이상의 모든 회원 제거
        queryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();
    }

    @Test
    public void sqlFunction(){//username이 member있것을 M으로 변경
        List<String> fetch = queryFactory//String 가반(숫자는 number)
                .select(Expressions.stringTemplate("function('replace',{0},{1},{2})"
                        , member.username, "member", "M"))
                .from(member)
                .fetch();
        for (String s : fetch) {
            System.out.println("s = " + s);
        }/*
        s = Member1
        s = M2
        s = Member3
        s = M4*/
    }
    @Test
    public void sqlFunction2(){//모든 문자사 소문자인 username을 출력
        List<String> fetch = queryFactory
                .select(member.username)
                .from(member)
//                .where(member.username.eq(Expressions.stringTemplate("function('lower',{0})"
//                        , member.username)))
                .where(member.username.eq(member.username.lower()))
                .fetch();//위 sql과 동일
        for (String s : fetch) {
            System.out.println("s = " + s);
        }
        /*
        s = member2
        s = member4*/
    }

}
