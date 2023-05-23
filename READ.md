## Spring Batch


### BATCH_JOB_INSTANCE
<hr>
Job parameter에 따라 생성되는 테이블
Spring Batch가 실행될 때 외부에서 받을 수 있는 파라미터
ex) 특정 날짜를 Job Parameter로 넘기면 해당 날짜 데이터로 조회, 가공, 입력 등의 작업을 할 수 있습니다.

같은 배치 잡이라도 Job Parameter가 다르면 BATCH_JOB_INSTANCE에 기록됩니다.
동일한 JOB이 Job Parameter가 달라지면 그때마다 생성되며, 동일한 Job Parameter는 여러개가 존재할 수 없습니다.


### BATCH_JOB_EXECUTION
<hr>
BATCH_JOB_EXECUTION와 BATCH_JOB_INSTANCE는 부모자식관계.
BATCH_JOB_EXECUTION 은 BATCH_JOB_INSTANCE가 성공/실패 했던 모든 내역을 가지고 있습니다.



### next()
<hr>
`next()`는 순차적으로 step들 연결시킬 때 사용됩니다.

> spring.batch.job.names: ${job.name:NONE}

스프링 배치가 실행될 때, 프로그램 arguments로 job.name 값이 넘어오면 해당 값과 일치하는 Job만 실행하겠다는 것

<b>job.name이 있으면 값을 할당하고 없으면, NONE을 할당하겠다는 의미</b> => 혹시라도 값이 없을 때 모든 배치가 실행되지 않도록 막는다 라는 의미입니다.

### 조건별 흐름제어 (FLOW)
<hr>
위의 next()는 스텝 단계에서 오류가나면 나머지 스텝은 진행되지 않습니다.

```java
   @Bean
    public Job stepNextConditionalJob() {
        return jobBuilderFactory.get("stepNextConditionalJob")
                .start(conditionalJobStep1())
                    .on("FAILED")// FAILED 일 경우
                    .to(conditionalJobStep3())// step3으로 이동
                    .on("*")// step3의 결과와 상관없이
                    .end() // step3으로 이동하면 Flow가 종료된다.
                .from(conditionalJobStep1()) // step1로부터
                    .on("*")// FAILED 외 모든 경우에
                    .to(conditionalJobStep2()) // step2 이동
                    .next(conditionalJobStep3()) // step2가 완료되면 step3
                    .on("*") // step3 결과에 상관없이
                    .end() // 종료
                .end().build(); // job 종료
    }
```


### Decide
<hr>
위의 스텝에서 두 가지 문제가 있습니다.
1. Step이 담당하는 역할이 2개 이상이 된다.
    - 실제 해당 Step이 처리해야할 로직외에도 분기처리를 시키기 위해 ExitStatus 코드 조작도 필요
2. 다양한 분기 로직 처리의 어려움
    - ExitStatus를 커스텀하게 고치기 위에서 Listener를 생성하고 Job Flow에 등록해야한다.

Step 들의 플로우 속에서 분기만 담당하는 타입이 있습니다.
`JobExecutionDecider`

> --job.name=deciderJob version=1



### JobParameter 와 Scope
<hr>

스프링 배치의 경우 외부 혹은 내부에서 파라미터를 받아 Batch 컴포넌트에서 사용할 수 있게 지원하고 있습니다.
이 파라미터를 JobParameter 라고 합니다.

JobParameter를 사용하기 위해선 항상 SpringBatch 전용 Scope를 선언해야합니다.
2가지가 있습니다.
    - @StepScope
    - @JobScope
SpEL로 선언해서 사용해야합니다.

@JobScope Bean의 생성시점을 지정된 Scope가 실행되는 시점으로 지연시킵니다.
> request scope와 비슷합니다. 요청이 왓을 때 생성이되고, 응답을 반환하면 삭제되는 것처럼, JobScope, StepScoe 역시 Job이 실행되고 끝날 떄 생성/삭제가 이루어집니다.

장점 2가지
    - JobParameter 의 Late Binding이 가능
        - JobParamter가 StepContext 또는 JobExecutionContext 레벨에서 할당시킬 수 있습니다. 꼭 application 시점이 아니더라도 Controller, Service 같은 비즈니스 로직 처리단계에서 할당할 수 있습니다.
    - 동일한 컴포넌트를 병렬 혹은 동시에 사용할 때 유용합니다.
        - Step 속 tasklet이 있고, tasklet 은 멤버변수와 이 멤버변수를 변경하는 로직이 있다고 생각해보겠습니다.
        - @StepScope 없이 Step을 병렬로 실행시키게 되면 서로 다른 Step에서 하나의 Tasklet을 두고 마구잡이 상태를 변경하려고 합니다. 하지만 @StepScope가 각각의 Step에서 별도의 Tasklet을 생성하고 관리하기 때문에 서로의 상태를 침범할 수 없습니다.


> 스프링 빈을 메소드나 클래스 어느것을 통해서 생성해도 무관하나 Bean의 scope는 Step 또는 Job 이어야 합니다.

### Chunk
<hr>
SpringBatch에서 Chunk는 데이터 덩어이로 작업할 때 각 커밋사이에서 처리되는 row 수를 의미합니다.
즉, 한번에 하나씩 데이터를 읽어 Chunk라는 덩어리를 만든 뒤, Chunk 단위로 트래잭션을 다루는 것

Chunk 단위로 트랜잭션을 수행하기 때문에 실패할 경우 해당 Chunk만큼만 롤백이 되고  이전에 커밋된 트랜잭션 범위까지는 반영이 됩니다.

> Reader와 Processor 에선 한 건씩 다뤄지고, Writer 에서는 Chunk 단위로 처리됩니다.
```java
for(int i=0; i<totalSize; i+=chunkSize){ // chunkSize 단위로 묶어서 처리
        List items = new Arraylist();
        for(int j = 0; j < chunkSize; j++){
        Object item = itemReader.read()
        Object processedItem = itemProcessor.process(item);
        items.add(processedItem);
        }
        itemWriter.write(items);
}
```


### ItemReader
스프링 배치는 ItemReader 를 통해 데이터를 읽어옵니다. (DB 뿐만아니라 XML, JSON...)
- 입력 데이터에서 읽어오기
- 파일에서 읽기
- 디비에서 읽기
- 자바 Message Service에서 읽기
- 커스텀으로 읽기




## 참고
[실습한 자료](https://jojoldu.tistory.com/)