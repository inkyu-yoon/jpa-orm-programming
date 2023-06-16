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


<details>

<summary><h2> Chapter 3. 영속성 관리 </h2></summary>

<details>

<summary><h3> 영속성 컨텍스트란? (Persistence Context) </h3></summary>

엔티티의 상태를 관리하고 다양한 영속성 관리 기능을 제공하는 논리적인 영역이다.

영속성 컨텍스트는 `EntityManager` 를 생성할 때 만들어지며, `EntityManager` 를 통해서 영속성 컨텍스트에 접근할 수 있다.

</details>

<details>

<summary><h3> 엔티티 상태의 종류와 특징 </h3></summary>

엔티티에는 4가지 상태가 존재한다.

1. 영속 : `persist()` 되어 엔티티 매니저에 의해 영속성 컨텍스트에 보관된다. 영속성 컨텍스트에 의해 관리되고 있는 상태이다.
2. 비영속 : `persist()` 하기 전, 영속성 컨텍스트와 아무런 관련이 없는 상태를 말한다.
3. 준영속 : `detach()` 되어 영속성 컨텍스트가 관리하다가 분리되어 있는 상태를 말한다. `close()` 되거나 `clear()` 되어 초기화되는 경우도 준영속 상태가 된다. `merge()` 로 다시 영속 상태로 만들 수 있다.
4. 삭제 : `remove()` 되어 영속성 컨텍스트에서 제외된 상태이다. 다시 `persist()` 해서 영속 상태를 만들 수 있다.

준영속 상태는 1차 캐시에 엔티티의 식별자(identifier)를 유지하고 있어, 이를 통해 다시 영속 상태로 전환하여 영속성 컨텍스트로 관리할 수 있다.

</details>

<details>

<summary><h3> 영속성 컨텍스트의 특징 </h3></summary>



1️⃣ **1차 캐시**

영속성 컨텍스트는 내부에 캐시를 가지고 있다. 이것을 1차 캐시라 한다.

`@Id` 어노테이션으로 매핑한 값을 기준으로 엔티티를 식별한다.

<br>

조회 요청 시, **1차 캐시에서 식별자 값으로 엔티티를 찾는다.**

만약, **찾는 엔티티가 있으면 DB를 조회하지 않고 메모리에 있는 1차 캐시에서 엔티티를 조회**한다.

<br>

즉, 영속화되어 영속성 컨텍스트에 의해 관리되고 있는 엔티티는 1차 캐시에 존재하고,

엔티티 조회 시 DB에 요청하지 않으므로 성능상 이점이 있다.

<br>

그리고 JDBC를 사용했을 때에는, 같은 식별자를 가진 엔티티를 조회하더라도 조회한 `ResultSet` 에서 엔티티 객체로 매핑해서 반환받으므로

참조 주소값이 다른, 동일성이 일치하지 않는 결과가 발생한다.

하지만, 영속성 컨텍스트는 엔티티가 영속화 되는 순간 해당 엔티티의 식별자로 하는 요청은 1차 캐시에 이미 존재하는 엔티티인지 확인하고

있다면 1차 캐시에 있는 엔티티를 반환하므로 항상 동일함을 보장할 수 있다.

<br>

2️⃣ 쓰기 지연

엔티티 매니저는 **트랜잭션을 커밋하기 직전**까지 내부 쿼리 저장소에 SQL을 모아둔다.

그리고 **트랜잭션을 커밋할 때 모아둔 쿼리를 데이터베이스에 보낸다.**

쿼리를 모아서 한번에 전달하는 방식은 성능상 이점이 있다.

<br>

3️⃣ 변경 감지

**엔티티의 변경사항을 데이터베이스에 자동으로 반영**하는 기능이다.

영속화되어 1차 캐시에 엔티티가 등록될 때, **최초 상태의 엔티티 정보**를 따로 보관해둔다. 이를 **스냅샷**이라고 한다.

그리고, 트랜잭션 커밋 시점에 **1차 캐시에서 스냅샷과 비교했을 때 달라진 엔티티가 있는지 확인**한다.

변경된 엔티티가 있는 경우 **수정 쿼리를 내부적으로 생성**해서 쓰기 지연 쿼리 저장소에 보낸다.

<br>

JPA는 `UPDATE 엔티티 SET (모든 필드) ..` 와 같이 모든 필드를 대상으로 업데이트 쿼리를 작성하고, 변경된 부분만 변경된 데이터로 바인딩하는 방식이 기본 전략이다.

변경되어 바인딩 되는 데이터를 제외하고 항상 수정 쿼리문이 같아서 파싱된 쿼리를 재사용할 수 있다는 장점이 있다.

<br>

만약 수정된 데이터만 동적으로 sql을 생성하는 전략을 사용하고 싶다면

엔티티 클래스에 `@org.hibernate.annotations.DynamicUpdate` 어노테이션을 붙이면 된다.

엔티티의 필드가 많은 경우, 동적으로 생성하는 방식이 더 성능이 좋은 경우가 있지만 테스트 후 적용하는 것이 좋다.



</details>


</details>



<details>

<summary><h2> Chapter 4. 엔티티 매핑 </h2></summary>

<details>

<summary><h3> @Entity</h3></summary>

- `(name = "엔티티명")` : JPA에서 사용할 엔티티 이름을 지정한다. 기본값은 클래스 이름이다.
- 파라미터가 없는 기본 생성자는 필수이다. 자바는 생성자가 아예 없는 경우 기본 생성자를 자동으로 만들지만, 파라미터가 있는 생성자가 하나라도 있을 경우 자동으로 만들어주지 않기 때문에 직접 만들어줘야 한다.
- 저장할 필드에는 `final` 키워드를 붙여주면 안된다.

</details>

<details>

<summary><h3> @Table</h3></summary>

`(name = "엔티티명")` : 매핑할 테이블 이름을 지정한다. 생략했을 때 기본값은 엔티티 이름이다. 만약 `@Entity` 의 `name` 옵션을 사용했다면, 옵션으로 지정한 이름이 엔티티 이름이 되므로, 매핑되는 테이블 이름도 옵션으로 지정한 이름이 된다.

</details>


<details>

<summary><h3> 기본키 전략 @GeneratedValue(strategy = GenerationType.{전략})</h3></summary>

어노테이션을 적용해서 적절한 전략을 선택할 수 있다.

1️⃣ 직접 할당

- 위 어노테이션을 사용하지 않은 경우이다. 영속화 하기 전에 직접 `setId()`와 같은 방식으로 식별자 값을 입력해주어야 한다.

2️⃣ **IDENTITY**

- 기본 키 생성을 DB에 위임하는 전략이다. MySQL의 경우 AUTO_INCREMENT 기능이 사용된다.
- 엔티티가 영속 상태가 되려면, **식별자는 반드시 필요**하다. 따라서, IDENTITY 전략은 DB로 부터 기본키 값을 얻어와야하기 때문에 **엔티티가 영속화 되는 시점에 바로 `INSERT` 쿼리를 실행해서 기본 키를 할당받고 DB로부터 조회하는 작업이 수반**된다.

3️⃣ **SEQUENCE**

-  유일한 값을 순서대로 생성하는 데이터베이스의 시퀀스를 사용한다. 시퀀스를 지원하는 DB에서 사용할 수 있다.

```sql
CREATE SEQUENCE BOARD-SEQ START WITH 1 INCREMENT BY 1;
```

위와 같이 시퀀스를 생성하고

```java
@Entity
@SequenceGenerator(
	name = "BOARD_SEQ_GENERATOR",
	sequenceName = "BOARD_SEQ", // 매핑할 데이터베이스 시퀀스 이름
	initialValue = 1, // DDL 생성 시에만 사용된다. 처음 시작하는 수
    allocationSize = 1 // 시퀀스 한 번 호출에 증가하는 수
)
public class Board{
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE
					generator = "BOARD_SEQ_GENERATOR")
	public Long id;
	
}
```

위와 같이 클래스 레벨에 붙인 어노테이션인 `@SeqeunceGenerator` 로 생성한 시퀀스와 매핑한다.

그리고 기본키 전략으로 `SEQUENCE`를 선택하고 생성한 시퀀스를 `generator` 옵션으로 지정한다.

> allocationSize의 값과 `hibernate.id.new_generator_mappings = true` 설정을 통해서 최적화하는 방법이 있다.
>
> 예로, allocationSize = 50인 경우, 시퀀스를 한번에 50 증가시켜 1~50 까지는 **메모리**에서 식별자를 할당한다.
>
> 시퀀스 값을 선점하므로, 여러 JVM이 동시에 동작해도 기본 키 값이 충돌하지 않는 장점이 있다.

4️⃣ **TABLE**

키 생성 전용 테이블을 하나 만들고 데이터베이스 시퀀스를 흉내내는 전략이다.

```mysql
CREATE TABLE MY_SEQUENCES (
	SEQUENCE_NAME VARCHAR(255) NOT NULL,
	NEXT_VAL BIGINT,
	PRIMARY KEY (SEQUENCE_NAME)
)
```

`SEQUENCE_NAME`을 시퀀스 이름으로 사용하고 `NEXT_VAL` 을 시퀀스 값으로 사용한다.

<br>

```java
@Entity
@TableGenerator(
	name = "BOARD_SEQ_GENERATOR",
    table = "MY_SEQUENCE",
    pkColumnValue = "BOARD_SEQ", // 키로 사용할 값 이름
    allocationSize = 1
)
public class Board{
    
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE,
                   generator = "BOARD_SEQ_GENERATOR")
    private Long id;
}
```

위와 같이 `@TableGenerator` 어노테이션을 통해서, 시퀀스 용으로 만든 테이블명과 기본

5️⃣ **AUTO**

선택한 데이터 베이스 방언에 따라 전략 중 하나를 자동으로 선택한다.

예로, Oracle의 경우 `SEQUENCE`를 MySQL의 경우 `IDENTITY`를 사용한다.



💡 **결론**

영속성 컨텍스트가 엔티티를 관리하기 위해서는 식별자로 식별해야한다.

직접 할당의 경우, **식별자 값을 개발자가 직접 할당**한다.

**IDENTITY** 의 경우, 데이터베이스에 기본키 생성을 위임하기 때문에 **DB에 데이터를 입력 후 조회**해온다.

**TABLE** 이나 **SEQUENCE** 전략의 경우 **AllocationSize에 따라 메모리에서 조회하거나 DB에서 조회**한다.




</details>



<details>

<summary><h3> 자연 키와 대리 키</h3></summary>

1. 자연 키 : 비즈니스에 의미가 있는 키를 의미한다. 예로, 주민등록번호, 전화번호 등이 있다.
2. 대리 키 : 비즈니스와 관련 없는 임의로 만들어진 키를 의미한다. Auto_increment에 의해 만들어진 키가 대리 키에 해당한다.

웬만하면 자연 키 보다는 대리 키를 사용하는 것이 좋다.

자연 키는 미래에 변화가 생길 수 있는 여지가 있기 때문이다.

김영한 개발자님의 경험에 의하면, 주민등록번호를 키 로 사용하다가 정부 정책 때문에 사용하지 못하도록 되어서 수정할 부분이 엄청나게 많았다고 하셨다.

비즈니스에 무관한 키는 변경될 일이 없다.

</details>


</details>


<details>

<summary><h2> Chapter 5. 연관관계 매핑 기초 </h2></summary>

<details>

<summary><h3> 객체 연관관계와 테이블 연관관계</h3></summary>


객체 연관관계와 테이블 연관관계 개념의 차이는, 객체 관계 불일치 패러다임의 대표적인 사례 중 하나이다.

`Member` 와 `Team` 이 다대일 관계라고 할 때, 테이블의 경우 외래키를 기준으로 양방향으로 탐색이 가능하다.

외래키로 지정된 값은 `Member` 테이블이나 `Team `테이블이나 같은 값을 갖기 때문이다.

하지만, 객체의 경우 양방향 관계를 맺기 위해서, `Member` 에서는 `Team`을 객체로 갖고 있고, `Team`은 `Member`를 `List<Member>` 와 같은 형태로 갖고 있어야 한다.

그리고 `List<Member>` 에서 원하는 `Member`를 찾았을 지라도, 동등성(객체의 속성이 일치)은 성립해도 동일성(주소값이 일치)은 만족하지 못한다.

따라서, 양방향 관계라고 하기보다는 2개의 단방향 관계라고 볼 수 있다.

<br>

그리고 데이터베이스에서 `일대다` 관계에서는 항상 `다` 쪽이 외래 키를 가진다.

JPA에서도 마찬가지로 `@ManyToOne`를 사용하는 객체는 일대다 관계에서 다에 해당한다.

따라서, **연관관계의 주인(외래키를 갖는)이 아님을 나타내는 `mappedBy` 옵션이 존재하지 않는다.**

참고로, `mappedBy` 옵션이 있으면 데이터를 읽는 것만 가능하다.

<br>

양방향 관계에서는, 연관관계의 주인(mappedBy 옵션이 없는, 일반적으로 다대일 관계에서 다)인 객체만이 외래키를 수정하거나 변경할 수 있지만

객체 관점에서 봤을 때, 양방향 관계라면 연관관계의 주인인 객체에 외래키와 관련된 객체를 추가하거나 삭제한다면

연관관계의 주인이 아닌 객체에도(일반적으로 List의 형태로 연관관계의 주인인 객체를 보관) 제거하거나 추가해주는 것이

객체 관점에서 안전하다.

물론, 변경이나 추가 이후에 별다른 작업이 없다면 DB에는 오류없이 반영되고 반영된 DB로 조회해서 사용하기 때문에 괜찮다.

</details>


<details>

<summary><h3> 연관관계 제거</h3></summary>

연관된 엔티티를 삭제하려면 기존에 있던 연관관계를 먼저 제거하고 삭제해야한다.

그렇지 않으면 외래 키 제약조건으로 인해, 데이터베이스에서 오류가 발생한다.

```java
// member1 과 member2가 team1 과 연관관계를 맺을 때, team1을 삭제하는 경우
member1.setTeam(null);
member2.setTeam(null);

em.remove(team1);
```


</details>
</details>



<details>

<summary><h2> Chapter 6. 다양한 연관관계 매핑 </h2></summary>

<details>

<summary><h3> 일대다 관계에서 연관관계 주인을 다로 설정하는 이유</h3></summary>

먼저, 관계형 테이블에서도 일대다 관계에서는 외래키(일반적으로 일쪽의 기본키)를 `다` 가 관리한다.

JPA에서도, 연관관계 주인을 `@ManyToOne` 어노테이션을 사용하는 `다` 에 해당하는 엔티티에 `@JoinColumn` 어노테이션을 통해서 설정한다.

<br>

`일` 쪽에도 연관관계 주인을 설정할 수 있다.

다만, `다` 쪽에서 외래키를 관리하는 경우 엔티티를 db에 insert 할 때, 외래키 값을 한번에 insert 할 수 있지만

`일` 쪽에서 관리하는 경우 `다`에 해당하는 엔티티에 먼저 외래키 값을 null 로 하는 데이터를 채운 뒤,  

`일` 에 해당하는 엔티티의 기본키값을 update하는 쿼리를 날리므로 성능이 저하될 수 있다.


</details>
</details>

<details>

<summary><h2> Chapter 7. 고급 매핑 </h2></summary>

<details>

<summary><h3> 상속관계 매핑</h3></summary>



1️⃣ 조인 전략

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "DTYPE")
public abstract class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long id;

    private String name;
    private int price;

}
```

위와 같이 데이터베이스의 Super-Sub 구조에서 Super에 해당하는 상위 테이블에 `@Inheritance(strategy = InheritanceType.JOINED)`  어노테이션을 붙인다.

`@DiscriminatorColumn(name = "DTYPE")` 은 부모 클래스에 생기는 속성명으로, 하위 테이블을 구분할 수 있다.

<br>

```java
@Entity
@DiscriminatorValue("A")
public class Album extends Item {
    private String artist;
}

```

하위 테이블은 `abstract` 클래스인 상위 엔티티 `Item` 을 상속한다.

`@DiscriminatorValue()` 로 지정한 값으로 상위 테이블의 `@DiscriminatorColumn`로 지정한 속성명에 값으로 입력된다.



<img src="https://raw.githubusercontent.com/buinq/imageServer/main/img/image-20230616120313009.png" alt="image-20230616120313009" style="zoom: 67%;" />

데이터는 위와 같이, 부모 테이블의 `dtype` 속성에는 하위 테이블에서 구분자로 설정한 `A` 가 입력된다.

그리고 하위 테이블에는 부모테이블의 기본키가 외래키 & 기본키 로서 사용된다.

만약 하위 테이블의 `item_id` 속성명을 변경하고 싶다면 `@PrimaryKeyJoinColumn` 을 사용하면 된다.

<br>

하지만 위와 같은 테이블 구조는 조회 시 조인이 필요하고 쿼리가 복잡하다.

그리고 테이블 2개에 각각 데이터를 입력해야하므로 insert 쿼리가 2번 발생한다.

<br>

2️⃣ 단일 테이블 전략

단일 테이블 전략은 `@Inheritance(strategy = InheritanceType.SINGLE_TABLE)` 어노테이션을 사용하면 된다.

<img src="https://raw.githubusercontent.com/buinq/imageServer/main/img/image-20230616120857093.png" alt="image-20230616120857093" style="zoom:67%;" />

단일 테이블 전략은, 테이블이 하나만 생성되고 하위 엔티티와 상위 엔티티의 모든 속성이 한 테이블에 존재하게 된다.

한 테이블에 같이 있기 때문에, `@DiscriminatorColumn` 으로 레코드간 구분할 수 있도록 해야하며

하위 엔티티 마다 갖고 있는 속성이 다르기 때문에, null 데이터가 존재하게 된다.

<img src="https://raw.githubusercontent.com/buinq/imageServer/main/img/image-20230616121319665.png" alt="image-20230616121319665" style="zoom:80%;" />

예를 들어, 책에는 `artist` 필드가 없고 앨범에는 `isbn` 필드가 없기 때문에 null 값이 테이블에 존재하게 된다.

조인이 필요없다는 장점이 있지만, 속성이 너무 많아지면 조회 성능이 느려질 수 있고 null 을 허용해야한다.



<br>

3️⃣ 구현 클래스마다 테이블 전략

`@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)` 어노테이션을 사용하면 된다.

이 전략은 하위 엔티티마다 테이블을 생성한다.

</details>

<details>

<summary><h3> @MappedSuperclass</h3></summary>

공통되는 속성을 엔티티에 제공할 수 있다.

예를 들어 레코드 별 데이터 입력 시간과 삭제 시간 칼럼이 필요한 경우

엔티티 마다, `createdDate` 필드를 각각 추가하기보다는

`createdDate` 필드를 가진 `BaseEntity` 클래스를 하나 생성하고 `@MappedSuperclass` 어노테이션을 사용한다.

그리고 이 어노테이션이 적용된 `BaseEntity` 를 상속하도록 하면, DDL에 의해 생성된 각 테이블에는 `BaseEntity` 에 정의된 속성이 생성되게 된다.

</details>

<details>

<summary><h3> 복합키</h3></summary>

복합키란, 기본키의 역할을 하는 컬럼이 2개 이상인 것을 말한다.

JPA에서 복합키를 사용하기 위해서는 `Serializable` 인터페이스를 구현하는 식별자 클래스를 따로 정의해야한다.

그리고, 조회 시 `em.find(엔티티.class, 식별자 인스턴스)` 로 조회할 수 있다.

식별자 인스턴스는 복합키에 해당하는 필드에 적절한 값이 입력되어 있어야 한다.

`@IdClass` 혹은 `@EmbeddedId` 어노테이션을 활용해서 구현할 수 있다.

</details>

<details>

<summary><h3> 식별 관계 vs 비식별 관계</h3></summary>

먼저, 식별 관계는 상위 테이블의 키를 하위 테이블의 기본키로 계속 전달하는 것이고

비식별 관계는 상위 테이블의 키를 외래키로서 속성으로 사용하는 관계이다.

<br>

식별 관계는 하위 테이블로 내려갈 수록 복합키의 크기가 커지고 변화가 필요한 경우 전파되는 영역이 커진다는 단점이 있다.

하지만, 기본 키 인덱스를 활용해서 조회 성능에서 이점이 있는 경우도 있다.

</details>
</details>


<details>

<summary><h2> Chapter 8. 프록시와 연관관계 관리 </h2></summary>

<details>

<summary><h3> 지연 로딩</h3></summary>

객체가 연관관계가 있는 객체의 정보는 사용하지 않는 경우가 있다.

이런 경우에도 연관관계가 있는 객체의 정보까지 `join` 해서 가져온다면 효율적이지 않다.

```java
@Getter
@Entity
public class Member {

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
}
```

위와 같은 `Member` 엔티티에서 `member.getTeam().getTeamName()` 과 같이 연관된 객체를 구체적으로 활용하는 경우에만 추가적으로 쿼리를 날려서 정보를 가져오는 **지연로딩** 기능을 제공한다.

<br>

이러한 방법은 프록시 객체를 사용함으로서 가능해진다.

`Team` 객체를 상속해서 같은 메서드를 갖는 가짜 객체를 생성한다.

그리고 그 가짜 객체가 `Member` 안에 포함되어 있고, 가짜 객체의 메서드를 호출하는 시점에서 DB에 조회 쿼리를 전달하고 실제 정보를 반환하는 방식이다.

<br>

1. 엔티티의 필드 정보를 요청한다.
2. 프록시 객체 상태인 경우, 데이터가 존재하지 않기 때문에 영속성 컨텍스트에 엔티티 생성을 요청한다.
3. 영속성 컨텍스트는 DB에 조회 쿼리를 실행시켜 엔티티 객체를 생성한다.
4. 프록시 객체는 생성된 엔티티 객체를 타겟으로 초기화 하고, 타겟이 가진 메서드를 실행시켜 결과를 반환한다.

위와 같은 방식으로 지연 로딩이 이루어진다.

</details>

<details>

<summary><h3> 식별자 값을 이용한 쿼리 최적화</h3></summary>

연관관계를 맺을 객체의 식별자를 알고 있다면, 추가적인 쿼리를 보내지 않을 수 있다.

데이터베이스상, 외래키를 갖는 객체의 테이블에는 외래키만 입력해주면 된다.

따라서

```java
@Getter
@Entity
public class Member {

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;
}
```

위와 같은 엔티티에서

<br>

```java
Member member = em.find(Member.class, "member1");
Team team = em.getReference(Team.class, "team1");
member.setTeam(team);
```

과 같은 방식으로 team을 식별자로서 프록시 객체로 초기화 하고

member 에 연관관계를 맺어주면, team을 실제 db에서 조회하지 않고 연관관계를 맺어줄 수 있다.

<br>

```java
Member member = em.find(Member.class, "member1");
Team team = em.find(Team.class, "team1");
member.setTeam(team);
```

만약 위와 같았다면, team을 db에서 조회해서 연관관계를 맺어주므로 select 쿼리가 2번 발생하게 된다.



</details>

<details>

<summary><h3> 즉시 로딩과 지연 로딩</h3></summary>

**즉시 로딩**은 엔티티를 조회할 때 연관된 엔티티도 함께 조회한다.

보통, 즉시 로딩을 최적화 하기 위해 가능하면 조인 쿼리를 사용해서 조회한다.

<br>

**지연 로딩**은 연관된 엔티티를 실제 사용할 때 조회한다.

<br>

**즉시 로딩**의 경우 연관된 객체에 `(fetch = FetchType.EAGER)` 를 사용하고 지연 로딩의 경우 `(fetch = FetchType.LAZY)`를 사용한다.

<br>

일반적으로 **즉시 로딩을 사용하는 경우, OUTER 조인**을 사용한다.

그 이유는, `Member` 를 조회할 때, 외래키(`team_id`)가 존재하지 않는 엔티티가 있을 수 있고 **INNER 조인을 사용하는 경우 `Member` 엔티티를 조회할 수 없기 때문**이다.

이러한 경우는 외래키에 해당하는 속성이 null 을 허용하는 경우 발생하므로

```
@ManyToOne(fetch = FetchType.EAGER)
@JoinColumn(name = "team_id", nullable = false)
```

위와 같이 **외래키가 null인 경우는 존재하지 않는다**라는 것을 명시해주면

조회하는 엔티티가 **누락되는 경우가 존재하지 않게 되므로 INNER 조인을 사용**하게 된다.

<br>

즉시 로딩은 연관객체가 자주 사용되는 경우라면, 한번의 쿼리로 조인해서 가져오기 때문에 지연 로딩보다 좋다.

하지만, 자주 사용되지 않는 경우라면 불필요한 조인을 하기 때문에 지연 로딩을 하는 것이 좋다.

<br>

참고로, `@ManyToOne` 혹은 `@OneToOne` 의 경우 기본 전략이 즉시 로딩이고

`@OneToMany` 혹은 `@ManyToMany` 의 경우 기본 전략이 지연 로딩이다.



</details>

<details>

<summary><h3> 영속성 전이 (Cascade) </h3></summary>

연관관계가 존재하는 엔티티를 영속화 하기 위해서는 연관관계에 해당하는 모든 엔티티를 `persist()` 를 통해 영속화 시켜야한다.

```java
Member member = new Member();
Team team = new Team();

em.persist(team);

member.setTeam(team);
em.persist(member);
```

위와 같이 `Member`가 참조하는 `Team` 을 영속화 시킨 후, `Member`에 할당하고 `Member` 도 영속화 시킴으로서 DB에 반영된다.



JPA는 영속성 전이 기능을 통해서 한번에 영속화 할 수 있는 기능이 있다.

```java
@Entity
@Setter
public class Member {

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "team_id")
    private Team team;
}
```

위와 같이 `@ManyToOne(cascade = CascadeType.PERSIST)` 옵션을 붙이면

설정한 엔티티까지 함께 영속화해서 저장한다.

<br>

```java
Member member = new Member();
Team team = new Team();
member.setTeam(team);

em.persist(member);
```

따라서 위와 같은 코드에서도, `team` 객체를 `persist()`로 명시적으로 영속화 시키지 않아도 영속화가 이루어진다.

<br>

이와 유사하게 `(cascade = CascadeType.REMOVE)` 옵션을 사용하면 삭제를 전파할 수 있다.



만약 연관관계의 주인이 아닌 `다` 쪽에 `CasCadeType.PERSIST` 옵션을 붙이면

```java
Member member = new Member();
Team team = new Team();
team.getMembers().add(member);

em.persist(team);
```

위와 같은 코드는 `member` 와 `team` 이 DB에 등록되지만, `member` 는 자신이 어떤 `team` 을 참조하는지 모르기 때문에 외래키에 `null` 이 입력된다.

따라서,

```java
Member member = new Member();
Team team = new Team();
member.setTeam(team);
team.getMembers().add(member);

em.persist(team);
```

위와 같이 `member` 에도 어떤 `team`을 참조하는지 세팅을 해주어야 한다.

따라서, 연관관계의 주인(일반적으로 일대다에서 `다` 쪽)에 영속성 전이 설정을 해두는 것이 더 편해보인다.

</details>

<details>

<summary><h3> 고아 객체 (orphanRemoval)</h3></summary>

고아 객체란 어디에서도 참조되지 않는 객체를 말한다.

예를 들어, `Team`과 `Member` 사이에서 `Team` 이 관리하는 `List<Member> members` 에서 특정 멤버를 제거하면 `member`를 참조하는 객체는 사라진다.

그러면 실제로 `delete` 쿼리를 날려 해당 `member` 데이터를 삭제한다.

<br>

다대일 관계에서 `다` 쪽에서 참조하는 `일` 에 해당하는 객체에는 적용될 수 없는게, 다른 엔티티가 참조하고 있으므로 참조가 사라진것이 아니기 때문이다.

</details>
</details>