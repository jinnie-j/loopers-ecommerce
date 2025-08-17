import http from 'k6/http';

const BASE = 'http://localhost:8080';
const BRAND_ID = '42';
const SORT = 'LATEST'; // LATEST | PRICE_ASC | LIKES_DESC

export const options = {
    scenarios: {
        warmup: { executor: 'constant-vus', vus: 10, duration: '30s', startTime: '0s' },
        steady: {
            executor: 'constant-arrival-rate',
            rate: 50, timeUnit: '1s',
            duration: '2m',
            preAllocatedVUs: 60, maxVUs: 100,
            startTime: '30s',
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<200', 'p(99)<400'],
    },
    discardResponseBodies: true,
    summaryTrendStats: ['avg','min','med','p(90)','p(95)','p(99)','max'],
};

export default function () {
    const qs = `?sort=${SORT}&page=0&size=20&brandId=${BRAND_ID}`;
    http.get(`${BASE}/api/v1/products/search${qs}`, { tags: { endpoint: 'products_list' } });
}
