import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://morupark-api.urssu.com/api';
const RAMP_UP_DURATION = __ENV.RAMP_UP_DURATION || '1m';
const STEADY_DURATION = __ENV.STEADY_DURATION || '5m';
const RAMP_DOWN_DURATION = __ENV.RAMP_DOWN_DURATION || '1m';
const TARGET_VUS = Number(__ENV.TARGET_VUS || 500);
const P95_MS = Number(__ENV.P95_MS || 2000);
const FAIL_RATE = Number(__ENV.FAIL_RATE || 0.05);
const STATUS_POLL_COUNT = Number(__ENV.STATUS_POLL_COUNT || 3);
const THINK_TIME_SEC = Number(__ENV.THINK_TIME_SEC || 1);

export const options = {
  stages: [
    { duration: RAMP_UP_DURATION, target: TARGET_VUS },
    { duration: STEADY_DURATION, target: TARGET_VUS },
    { duration: RAMP_DOWN_DURATION, target: 0 },
  ],
  thresholds: {
    http_req_failed: [`rate<${FAIL_RATE}`],
    http_req_duration: [`p(95)<${P95_MS}`],
  },
};

export default function () {
  const uniqueId = `${__VU}${__ITER}${Date.now() % 10000}`;
  const studentId = `2024${uniqueId}`.slice(0, 10);
  const phoneNumber = `010${uniqueId}`.slice(0, 11);

  const loginRes = http.post(
    `${BASE_URL}/auth/login`,
    JSON.stringify({ studentId, phoneNumber }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  const isLoginOk = check(loginRes, {
    'login: status is 200': (r) => r.status === 200,
    'login: has access token': (r) => !!r.json('accessToken'),
  });
  if (!isLoginOk) return;

  const accessToken = loginRes.json('accessToken');
  const queueHeaders = {
    Authorization: `Bearer ${accessToken}`,
    'Content-Type': 'application/json',
  };

  const queueRes = http.post(`${BASE_URL}/queues`, null, { headers: queueHeaders });
  const isQueueOk = check(queueRes, {
    'enqueue: status is 202': (r) => r.status === 202,
    'enqueue: has waiting token': (r) => !!r.json('waitingToken'),
  });
  if (!isQueueOk) return;

  const waitingToken = queueRes.json('waitingToken');
  const statusHeaders = {
    Authorization: `Bearer ${accessToken}`,
    'X-Waiting-Token': waitingToken,
    'Content-Type': 'application/json',
  };

  for (let i = 0; i < STATUS_POLL_COUNT; i += 1) {
    sleep(THINK_TIME_SEC);
    const statusRes = http.get(`${BASE_URL}/queues/status`, { headers: statusHeaders });
    check(statusRes, {
      'status: status is 200': (r) => r.status === 200,
      'status: has result': (r) => !!r.json('status'),
    });
    const status = statusRes.json('status');
    if (status === 'SUCCESS' || status === 'FAILED') break;
  }
}
