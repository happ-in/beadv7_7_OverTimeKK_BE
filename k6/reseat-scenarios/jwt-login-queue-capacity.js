import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8081';
const ALGO = __ENV.ALGO || 'HS384';
const USER_COUNT = Number(__ENV.USER_COUNT) || 50;
const POLL_INTERVAL_SECONDS = 1;
const QUEUE_WAIT_DEADLINE_MS = 120000;

const loginDuration = new Trend('login_duration', true);
const queueWaitDuration = new Trend('queue_wait_duration', true);

/**
 * 순간적으로 VU를 몰아넣는 게 아니라, 초당 요청 도착률(arrival rate)을 서서히 늘려가며
 * "어느 유입 속도부터 대기열이 계속 쌓이기만 하는지"(=서비스 감당 한계)를 찾기 위한 시나리오.
 * ramping-arrival-rate: VU 수가 아니라 "초당 몇 건 시작하는지"를 직접 제어한다.
 */
function stage(rate, startTime) {
    return {
        executor: 'ramping-arrival-rate',
        startRate: rate,
        timeUnit: '1s',
        preAllocatedVUs: 300,
        maxVUs: 2000,
        startTime: startTime,
        stages: [{ target: rate, duration: '2m' }],
        exec: 'login',
        tags: { rate: String(rate), algo: ALGO, phase: 'login' },
    };
}

export const options = {
    scenarios: {
        rate_2: stage(2, '0s'),
        rate_5: stage(5, '2m'),
        rate_10: stage(10, '4m'),
        rate_15: stage(15, '6m'),
        rate_20: stage(20, '8m'),
    },
};

export function login() {
    const enterStart = Date.now();

    const enterRes = http.post(`${BASE_URL}/api/users/login/queue/enter`, null, {
        tags: { name: 'POST /api/users/login/queue/enter' },
    });

    if (!check(enterRes, { 'queue enter status is 200': (r) => r.status === 200 })) {
        return;
    }

    let body = JSON.parse(enterRes.body).data;
    const token = body.token;
    let status = body.status;

    const deadline = Date.now() + QUEUE_WAIT_DEADLINE_MS;
    while (status === 'WAITING' && Date.now() < deadline) {
        sleep(POLL_INTERVAL_SECONDS);
        const statusRes = http.get(`${BASE_URL}/api/users/login/queue/status?token=${token}`, {
            tags: { name: 'GET /api/users/login/queue/status' },
        });
        if (statusRes.status !== 200) {
            break;
        }
        status = JSON.parse(statusRes.body).data.status;
    }

    queueWaitDuration.add(Date.now() - enterStart);

    if (!check(status, { 'admitted before deadline': (s) => s === 'READY' })) {
        return;
    }

    const userNum = Math.floor(Math.random() * USER_COUNT) + 1;
    const loginBody = JSON.stringify({
        username: `loadtest${userNum}`,
        password: 'password123!',
    });

    const res = http.post(`${BASE_URL}/api/users/login`, loginBody, {
        headers: { 'Content-Type': 'application/json', 'X-Admission-Token': token },
        tags: { name: 'POST /api/users/login' },
    });

    loginDuration.add(res.timings.duration);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });
}
