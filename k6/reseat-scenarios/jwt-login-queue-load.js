import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8081';
const ALGO = __ENV.ALGO || 'HS384';
const USER_COUNT = Number(__ENV.USER_COUNT) || 50;
const STAGE_DURATION = __ENV.STAGE_DURATION || '30s';
const POLL_INTERVAL_SECONDS = 1;
const QUEUE_WAIT_DEADLINE_MS = 60000;

const loginDuration = new Trend('login_duration', true);
const queueWaitDuration = new Trend('queue_wait_duration', true);

function stage(vus, startTime) {
    return {
        executor: 'constant-vus',
        vus: vus,
        duration: STAGE_DURATION,
        startTime: startTime,
        exec: 'login',
        tags: { vus: String(vus), algo: ALGO, phase: 'login' },
    };
}

export const options = {
    scenarios: {
        vus_10: stage(10, '0s'),
        vus_50: stage(50, '30s'),
        vus_100: stage(100, '60s'),
        vus_300: stage(300, '90s'),
        vus_500: stage(500, '120s'),
        vus_1000: stage(1000, '150s'),
    },
    thresholds: {
        'http_req_failed': ['rate<0.05'],
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
