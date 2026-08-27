import http from 'k6/http';
import { check } from 'k6';
import { Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8081';
const ALGO = __ENV.ALGO || 'HS384';
const TOKEN = __ENV.TOKEN;
const STAGE_DURATION = __ENV.STAGE_DURATION || '30s';

const verifyDuration = new Trend('verify_duration', true);

function stage(vus, startTime) {
    return {
        executor: 'constant-vus',
        vus: vus,
        duration: STAGE_DURATION,
        startTime: startTime,
        exec: 'verify',
        tags: { vus: String(vus), algo: ALGO, phase: 'verify' },
    };
}

export const options = {
    discardResponseBodies: true,
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

export function verify() {
    const res = http.get(`${BASE_URL}/api/users/me`, {
        headers: { Authorization: `Bearer ${TOKEN}` },
        tags: { name: 'GET /api/users/me' },
    });

    verifyDuration.add(res.timings.duration);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });
}