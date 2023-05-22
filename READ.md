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
