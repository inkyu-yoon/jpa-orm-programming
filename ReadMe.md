# ✍🏻 기록

## JPA

```
JPA를 사용해서 얻은 가장 큰 성과는 애플리케이션을 SQL이 아닌 객체 중심으로 개발할 수 있어서

생산성과 유지보수가 확연히 좋아졌고 테스트를 작성하기도 편리해진다.

이러한 장점 덕분에 버그도 많이 줄어들었다.

또한, 개발 중간에 데이터베이스 시스템이 바뀌었을때에도 코드를 거의 수정하지 않고 데이터베이스를 손쉽게 변경할 수 있었다.


- 前 우아한 형제들 CTO 김영한 님이 실무에서 느끼신 JPA의 장점 -
```


<details>

<summary><h2> Chapter 1. JPA 소개 </h2></summary>

<details>

<summary><h3> JPA를 사용하는 이유 </h3></summary>

JDBC를 사용하는 경우, 직접 문자열 형태의 sql 질의를 작성해야한다.

입력 작업을 위해서는 질의문에 적절한 파라미터를 바인딩하기 위해 객체의 필드 정보를 `get` 으로 조회해서 입력해준 뒤 `executeUpdate()` 를 실행시킨다.

조회 작업을 위해서는 sql 로 얻어온 값을 `ResultSet` 과 같은 객체로 받아 `getString("칼럼명")` 등의 메서드를 사용해서 속성 값을 꺼낸 뒤 사용해야한다.

<br>

이렇듯, 간단한 CRUD 작업에도 매번 sql 질의문을 작성하고 바인딩하는 작업들이 수반된다.

<br>

또한, 만약 `INSERT INTO MEMBER (MEMBER_ID, NAME) VALUES(?, ?)` 와 같은 쿼리문을 사용중이었다가

Member 테이블에 연락처 속성이 추가된다면

직접 문자열 형태의 질의문을

`INSERT INTO MEMBER (MEMBER_ID, NAME, TEL) VALUES(?, ?, ?)` 와 같이 수정해야하며 바인딩 하는 작업까지 추가해주어야 한다.

<br>

연관관계가 맺어진 상황에서도 문제가 있다.

`Member` 와 `Team` 이 연관관계가 있다고 가정해보자.

<br>

우리가 원하는 것은, `Member` 만 조회해도 연관된 `Team` 객체의 정보를 사용할 수 있어야 하지만

JDBC로 전달하는 쿼리가 Team과 Join하는 쿼리문이 잘 작성되어 있어야 가능하다.

<br>

💡 결론은, 간단한 CRUD 작업에도 단순하고 반복되는 작업이 수반되고 단순한 엔티티의 필드 추가 같은 상황 때문에 변경되고 확인해야하는 코드의 수가 많다는 것이다.  

또한, 작성한 SQL 구문에 심하게 의존하게 된다는 단점이 존재한다.

이러한 대부분의 문제들을 JPA를 사용하면 해결할 수 있다.


</details>


<details>

<summary><h3> 객체 지향 언어와 관계형 DB 패러다임의 불일치 (Object–relational impedance mismatch) </h3></summary>

객체 지향 언어와 관계형 데이터 베이스 간 개념적 차이 때문에 발생하는 문제를 의미한다.

<br>

1️⃣ **상속**

<br>

객체는 상속이라는 기능을 갖고 있지만, 관계형 데이터 베이스 테이블은 상속이라는 기능이 없다.

따라서, 상속을 표현하기 위해서 **관계형 데이터 베이스는 Super-Sub 타입 관계**를 사용해야한다.

<div align = "center">
<img src="https://raw.githubusercontent.com/buinq/imageServer/main/img/image-20230609145413730.png" alt="image-20230609145413730" style="zoom:80%;" />
</div>

위와 같이 표현할 수 있을텐데, 어쨌든 데이터를 조회하거나 입력하기 위해서는 나눠진 테이블 2개에 접근해야한다.

입력 시에는 부모 객체 필드만 따로 꺼내서 Super 테이블에만 넣어주어야 하고, 자식 객체 필드만 따로 꺼내서 Sub 테이블에 입력해주는 쿼리를 작성해야 할 것이다.

조회 시에도 마찬가지로, Super 테이블에서 받은 데이터와 Sub 테이블에서 받은 데이터를 하나의 객체로 매핑해주는 작업이 필요할 것이다.

<br>

2️⃣ **연관관계**

<br>

객체는 방향성이 있는 **참조**를 사용해서 다른 객체와 연관관계를 맺고

관계형 데이터 베이스의 경우 방향성이 없는 **외래키** 를 이용해서 다른 테이블과 연관관계를 맺는다.

<br>

따라서, `Member` 안에 `Team` 객체가 있는 경우

```java
class Member {
    Long id;
    Team team;
    String username;
    
    Team getTeam(){
        return this.team;
    }
}

class Team {
    Long id;
    String name;
}
```

위와 같이 클래스를 구성한 뒤

Insert 해줄 때에는, `INSERT INTO MEMBER (id, team_id,username) values(?, ?, ?)` 쿼리를 사용해야하기 때문에 `Member` 가 가진 `Team` 을 그대로 바인딩 시켜주면 안돼고 `member.getTeam().getId()` 와 같은 방식으로 입력해주어야 할 것이다.

조회할 때 역시, `memberId` 로 찾은 `Member` 데이터와 `teamId` 로 찾은 `Team` 데이터를 객체로 매핑한뒤

`member.setTeam(team)` 과 같은 메서드로 직접 객체를 입력해주어야 한다.

<br>

3️⃣ 객체 그래프 탐색

<br>

2번째 불일치와 유사한데, `Member member = memberDao.find(memberId)` 라는 코드만 보고

`member.getTeam()` 메서드를 사용했을 때, `team` 이 `null` 이 아님을 확신할 수 있을까?

<br>
    
만약 dao의 `find` 로직 안에 sql 질의가 `team`테이블과 조인하는 sql 구문이었다면 `team` 데이터가 존재함을 알 수 있을 것이다.

이렇듯, 직접 `DAO` 클래스를 찾아가서 `sql` 문을 확인해야 내가 원하는 연관된 객체의 데이터를 얻을 수 있는지 없는지를 알 수 있다.

<br>

4️⃣ **비교 방식**

<br>

데이터베이스의 경우 기본 키의 값으로 레코드를 구분하지만, 객체는 동일성과 동등성 비교 두가지가 있다.

**동일성** 비교는 객체의 주소값을 비교하는 것이고

**동등성** 비교는 객체가 가진 내부의 값을 비교하는 것이다.

<br>

jdbc를 통해, 조회한 데이터의 `ResultSet` 으로 구성한 `Member`는 같은 `memberId` 로 얻은 데이터라도 동등성은 성립하지만 동일성은 성립하지 않는다.

<br>

이러한 패러다임의 불일치를 JPA를 사용하면 해결할 수 있다.



</details>

</details>


<details>

<summary><h2> Chapter 2. JPA 시작 </h2></summary>

<details>

<summary><h3> JPQL(Java Persistence Query Language) 이란? </h3></summary>

JPA를 사용하면 개발자는 엔티티 객체를 중심으로 개발하고 데이터베이스에 대한 처리는 JPA에 맡겨야 한다.

JPA는 엔티티 객체를 중심으로 개발하므로 검색을 할 때도 테이블이 아닌 엔티티 객체를 대상으로 검색해야한다.

<br>

```java
List<Member> members = em.createQuery("select m from Member m", Member.class)
                		 .getResultList();
```

그럴때 사용할 수 있는 것이 **JPQL** 이다.

DB마다 쿼리 문법이 다르기 때문에, 추상화한 쿼리 문법이고

이 JPQL을 바탕으로 JPA는 **설정한 DB에 맞는 쿼리문을 생성하여 데이터베이스와 상호작용**한다.

<br>

🚨 참고로 JPQL은 대소문자를 명확히 구분한다.

</details>

</details>
