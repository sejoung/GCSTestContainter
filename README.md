# GCSTestContainer

## Bug Report: `spring.cloud.gcp.storage.host` Drops Port Number

This project demonstrates a bug in `GcpStorageAutoConfiguration` from **Spring Cloud GCP**, where the `verifyAndFetchHost()` method strips the port from the configured host URL.

### Affected Code

`GcpStorageAutoConfiguration.java` (line 94, 103-118) in `spring-cloud-gcp-autoconfigure`

```java
// line 94
storageOptionsBuilder.setHost(verifyAndFetchHost(this.host));
```

```java
// line 103-118
private String verifyAndFetchHost(String host) {
    URL url;
    try {
        url = new URL(host);
    } catch (MalformedURLException e) {
        throw new IllegalArgumentException(
            "Invalid host format: " + host
            + ". Please verify that the specified host follows the "
            + "'https://${service}.${universeDomain}/' format");
    }
    return url.getProtocol() + "://" + url.getHost() + "/";  // <-- BUG: port is lost
}
```

### Problem

`URL.getHost()` returns only the hostname **without** the port. As a result:

| Input | Expected | Actual |
|---|---|---|
| `http://localhost:54321` | `http://localhost:54321/` | `http://localhost/` |

The port is silently dropped, making it impossible to connect to an emulator running on a non-default port.

### Impact

This directly affects integration testing with **fake-gcs-server** + **Testcontainers**, where the container port is dynamically mapped:

```kotlin
val endpoint = "http://${gcsContainer.host}:${gcsContainer.getMappedPort(4443)}"
// e.g. "http://localhost:54321"

// After verifyAndFetchHost(), it becomes "http://localhost/" -> Connection refused
```

### Proposed Fix

Replace `url.getHost()` with `url.getAuthority()` to preserve the port:

```java
// Before (bug)
return url.getProtocol() + "://" + url.getHost() + "/";

// After (fix)
return url.getProtocol() + "://" + url.getAuthority() + "/";
```

`URL.getAuthority()` returns `host:port` (or just `host` when port is absent), correctly handling both cases.

### How to Reproduce

1. Run the test in this project:
   ```bash
   ./gradlew test
   ```
2. `TestGcsContainterConfiguration` starts a fake-gcs-server container on a random port
3. Sets `spring.cloud.gcp.storage.host=http://localhost:<random-port>`
4. `GcpStorageAutoConfiguration` processes the host and strips the port
5. The `Storage` client tries to connect to `http://localhost/` instead -> **Connection refused**

### Environment

- Spring Cloud GCP: 7.4.5
- Spring Boot: 3.5.6
- Testcontainers: 1.21.3
- fake-gcs-server: latest

---

## Bug Report: `spring.cloud.gcp.storage.host` Port Number Loss Issue (KR)

Spring Cloud GCP `GcpStorageAutoConfiguration` 클래스의 `verifyAndFetchHost()` 메소드에서 host URL의 포트 번호가 제거되는 버그를 증명하기 위한 프로젝트입니다.

### 영향받는 코드

`GcpStorageAutoConfiguration.java` (94행, 103-118행) - `spring-cloud-gcp-autoconfigure`

```java
// 94행
storageOptionsBuilder.setHost(verifyAndFetchHost(this.host));
```

```java
// 103-118행
private String verifyAndFetchHost(String host) {
    URL url;
    try {
        url = new URL(host);
    } catch (MalformedURLException e) {
        throw new IllegalArgumentException(
            "Invalid host format: " + host
            + ". Please verify that the specified host follows the "
            + "'https://${service}.${universeDomain}/' format");
    }
    return url.getProtocol() + "://" + url.getHost() + "/";  // <-- BUG: 포트가 누락됨
}
```

### 문제점

`URL.getHost()`는 호스트명만 반환하고 **포트를 포함하지 않습니다**. 따라서:

| 입력값 | 기대값 | 실제값 |
|---|---|---|
| `http://localhost:54321` | `http://localhost:54321/` | `http://localhost/` |

포트가 조용히 제거되어, 기본 포트가 아닌 포트에서 실행 중인 에뮬레이터에 연결할 수 없게 됩니다.

### 영향 범위

**fake-gcs-server** + **Testcontainers**를 사용한 통합 테스트에 직접적인 영향을 미칩니다. 컨테이너 포트가 동적으로 매핑되기 때문입니다:

```kotlin
val endpoint = "http://${gcsContainer.host}:${gcsContainer.getMappedPort(4443)}"
// 예: "http://localhost:54321"

// verifyAndFetchHost() 처리 후 "http://localhost/"가 됨 -> Connection refused
```

### 수정 제안

`url.getHost()`를 `url.getAuthority()`로 교체하여 포트를 보존해야 합니다:

```java
// 수정 전 (버그)
return url.getProtocol() + "://" + url.getHost() + "/";

// 수정 후 (수정안)
return url.getProtocol() + "://" + url.getAuthority() + "/";
```

`URL.getAuthority()`는 `host:port`를 반환하며, 포트가 없는 경우에는 `host`만 반환하므로 두 경우 모두 올바르게 처리됩니다.

### 재현 방법

1. 이 프로젝트의 테스트를 실행합니다:
   ```bash
   ./gradlew test
   ```
2. `TestGcsContainterConfiguration`이 랜덤 포트로 fake-gcs-server 컨테이너를 시작합니다
3. `spring.cloud.gcp.storage.host=http://localhost:<랜덤포트>`를 설정합니다
4. `GcpStorageAutoConfiguration`이 host를 처리하면서 포트를 제거합니다
5. `Storage` 클라이언트가 `http://localhost/`에 연결을 시도합니다 -> **Connection refused**

### 환경 정보

- Spring Cloud GCP: 7.4.5
- Spring Boot: 3.5.6
- Testcontainers: 1.21.3
- fake-gcs-server: latest
