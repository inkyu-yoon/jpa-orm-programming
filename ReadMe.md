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


