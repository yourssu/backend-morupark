import http from 'k6/http';
import { check, sleep } from 'k6';

// 테스트 설정 (Stages 기반 부하 시뮬레이션)
export const options = {
  stages: [
    { duration: '30s', target: 50 },  // 30초 동안 50명까지 늘림 (Warm-up)
    { duration: '2m', target: 100 },  // 2분 동안 100명까지 늘림
    { duration: '5m', target: 100 },  // 5분 동안 100명 유지 (Steady state)
    { duration: '1m', target: 0 },    // 1분 동안 서서히 종료 (Cool-down)
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'], // 1% 미만 에러 허용
    http_req_duration: ['p(95)<500'], // 95%의 요청은 500ms 이내 처리
  },
};

// 기본 설정
const BASE_URL = 'http://api.morupark.urssu.com/api'; // GCP Ingress 주소

export default function () {
  // 1. 로그인 (학번/전화번호 입력)
  // 가상 사용자(VU) 번호와 반복 횟수(Iter)를 조합해 고유 식별자 생성
  const studentId = `2024${__VU}${__ITER}`.slice(0, 10);
  const phoneNumber = `010${__VU}${__ITER}`.slice(0, 11);

  const loginPayload = JSON.stringify({
    studentId: studentId,
    phoneNumber: phoneNumber,
  });

  const headers = { 'Content-Type': 'application/json' };

  const loginRes = http.post(`${BASE_URL}/auth/login`, loginPayload, { headers });

  const isLoginOk = check(loginRes, {
    'login: status is 200': (r) => r.status === 200,
    'login: has access token': (r) => r.json('accessToken') !== undefined,
  });

  if (!isLoginOk) {
    console.error(`Login failed for VU: ${__VU}, Iter: ${__ITER}. Status: ${loginRes.status}`);
    return;
  }

  const accessToken = loginRes.json('accessToken');

  // 2. 대기열 진입
  const authHeaders = {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json',
  };

  const queueRes = http.post(`${BASE_URL}/queues`, null, { headers: authHeaders });

  const isQueueOk = check(queueRes, {
    'enqueue: status is 202': (r) => r.status === 202,
    'enqueue: has waiting token': (r) => r.json('waitingToken') !== undefined,
  });

  if (!isQueueOk) {
      return;
  }

  const waitingToken = queueRes.json('waitingToken');

  // 3. 대기열 상태 조회 (짧은 대기 후 1회 수행)
  sleep(1);

  const statusRes = http.get(`${BASE_URL}/queues/status?token=${waitingToken}`, { headers: authHeaders });

  check(statusRes, {
    'status: status is 200': (r) => r.status === 200,
    'status: has result': (r) => r.json('status') !== undefined,
  });

  // 각 VU 간의 1~2초 랜덤 지연 (Think Time)
  sleep(Math.random() * 1 + 1);
}
