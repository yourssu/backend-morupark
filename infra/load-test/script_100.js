import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 },
    { duration: '2m', target: 100 },
    { duration: '5m', target: 100 },
    { duration: '1m', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500'],
  },
};

const BASE_URL = 'http://api.morupark.urssu.com/api';

export default function () {
  // 1. 로그인
  const studentId = `2024${__VU}${__ITER}`.slice(0, 10);
  const phoneNumber = `010${__VU}${__ITER}`.slice(0, 11);

  const loginRes = http.post(`${BASE_URL}/auth/login`, JSON.stringify({
    studentId: studentId,
    phoneNumber: phoneNumber,
  }), { headers: { 'Content-Type': 'application/json' } });

  const isLoginOk = check(loginRes, {
    'login: status is 200': (r) => r.status === 200,
    'login: has access token': (r) => r.json('accessToken') !== undefined,
  });

  if (!isLoginOk) return;

  const accessToken = loginRes.json('accessToken'); 

  // 2. 대기열 진입 (이 단계에서는 waitingToken이 없으므로 Authorization만 보냄)
  const enqueueHeaders = {
    'Authorization': `Bearer ${accessToken}`,
    'Content-Type': 'application/json',
  };

  const queueRes = http.post(`${BASE_URL}/queues`, null, { headers: enqueueHeaders });

  const isQueueOk = check(queueRes, {
    'enqueue: status is 202': (r) => r.status === 202,
    'enqueue: has waiting token': (r) => r.json('waitingToken') !== undefined,
  });

  if (!isQueueOk) return;

  // 💡 드디어 여기서 waitingToken이 탄생함!
  const waitingToken = queueRes.json('waitingToken');

  // 3. 대기열 상태 조회 (이제 waitingToken을 헤더에 실어 보냄)
  sleep(1);

  const statusHeaders = {
    'Authorization': `Bearer ${accessToken}`,
    'X-Waiting-Token': waitingToken, // 💡 이제 사용할 수 있음
    'Content-Type': 'application/json',
  };

  const statusRes = http.get(`${BASE_URL}/queues/status`, { headers: statusHeaders });

  check(statusRes, {
    'status: status is 200': (r) => r.status === 200,
    'status: has result': (r) => r.json('status') !== undefined,
  });

  sleep(Math.random() * 1 + 1);
}