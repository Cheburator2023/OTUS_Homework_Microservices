import http from 'k6/http';
import { check, sleep, group } from 'k6';
import { Trend, Rate, Counter } from 'k6/metrics';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

// Кастомные метрики
const metrics = {
    createUser: new Trend('create_user_duration'),
    getUser: new Trend('get_user_duration'),
    updateUser: new Trend('update_user_duration'),
    deleteUser: new Trend('delete_user_duration'),
    listUsers: new Trend('list_users_duration'),
    errors: new Rate('errors'),
    requests: new Counter('total_requests'),
};

export const options = {
    scenarios: {
        ramp_load: {
            executor: 'ramping-arrival-rate',
            startRate: 5,
            timeUnit: '1s',
            stages: [
                { duration: '30s', target: 10 },
                { duration: '30s', target: 20 },
                { duration: '30s', target: 30 },
                { duration: '30s', target: 50 },
                { duration: '30s', target: 60 },
                { duration: '1m', target: 80 },
                { duration: '2m', target: 100 },
                { duration: '1m', target: 90 },
                { duration: '30s', target: 70 },
                { duration: '30s', target: 50 },
                { duration: '30s', target: 40 },
                { duration: '30s', target: 30 },
                { duration: '30s', target: 20 },
                { duration: '30s', target: 10 },
            ],
            preAllocatedVUs: 50,
            maxVUs: 100,
            gracefulStop: '30s',
        },
    },
    thresholds: {
        'errors': ['rate<0.15'],
        'http_req_duration{api:create}': ['p(95)<300'],
        'http_req_duration{api:get}': ['p(95)<200'],
        'http_req_duration{api:update}': ['p(95)<400'],
        'http_req_failed': ['rate<0.01'],
        'http_req_duration': ['p(95)<500'],
    },
};

function makeTag(name) {
    return { api: name };
}

const BASE_URL = 'http://arch.homework';
const API_PREFIX = '/api/v1/users';
const headers = { 'Content-Type': 'application/json' };

export default function () {
    let userId;
    let testUser;
    let testSuccessful = true;

    group('User API Full Test', function () {
        // Тест создания пользователя
        const createPayload = JSON.stringify({
            name: `user-${randomIntBetween(1, 100000)}`,
            email: `user-${randomIntBetween(1, 100000)}@test.com`
        });

        const createRes = http.post(`${BASE_URL}${API_PREFIX}`, createPayload, {
            headers,
            tags: makeTag('create')
        });

        metrics.requests.add(1);
        metrics.createUser.add(createRes.timings.duration);

        if (!check(createRes, {
            'Create status is 201': (r) => r.status === 201,
            'Response has ID': (r) => r.json().id !== undefined,
        })) {
            testSuccessful = false;
            console.error(`Create failed: ${createRes.status} ${createRes.body}`);
        }

        // Тест получения пользователя
        if (createRes.status === 201) {
            userId = createRes.json().id;
            testUser = createRes.json();

            const getRes = http.get(`${BASE_URL}${API_PREFIX}/${userId}`, {
                tags: makeTag('get')
            });

            metrics.requests.add(1);
            metrics.getUser.add(getRes.timings.duration);

            if (!check(getRes, {
                'Get status is 200': (r) => r.status === 200,
                'Returned correct user': (r) => r.json().id === userId,
            })) {
                testSuccessful = false;
            }
        }

        // Тест списка пользователей
        const listRes = http.get(`${BASE_URL}${API_PREFIX}`, {
            tags: makeTag('list')
        });

        metrics.requests.add(1);
        metrics.listUsers.add(listRes.timings.duration);

        if (!check(listRes, {
            'List status is 200': (r) => r.status === 200,
            'List not empty': (r) => r.json().length > 0,
        })) {
            testSuccessful = false;
        }

        // Тест обновления пользователя
        if (userId && testUser) {
            const updatedData = {
                name: testUser.name, // Сохраняем оригинальное имя
                email: `updated_${testUser.email}`
            };

            const updateRes = http.put(
                `${BASE_URL}${API_PREFIX}/${userId}`,
                JSON.stringify(updatedData),
                {
                    headers,
                    tags: makeTag('update')
                }
            );

            metrics.requests.add(1);
            metrics.updateUser.add(updateRes.timings.duration);

            if (!check(updateRes, {
                'update user status 200': (r) => r.status === 200,
                'email updated': (r) => {
                    try {
                        return r.json().email === updatedData.email;
                    } catch (e) {
                        console.error(`Update parse error: ${e.message}`);
                        return false;
                    }
                },
            })) {
                testSuccessful = false;
                console.error(`Update failed for user ${userId}: ${updateRes.status} ${updateRes.body}`);
            }

            // Тест удаления пользователя
            const deleteRes = http.del(`${BASE_URL}${API_PREFIX}/${userId}`, null, {
                headers,
                tags: makeTag('delete')
            });

            metrics.requests.add(1);
            metrics.deleteUser.add(deleteRes.timings.duration);

            if (!check(deleteRes, {
                'delete user status 204': (r) => r.status === 204,
            })) {
                testSuccessful = false;
            }
        }

        if (!testSuccessful) {
            metrics.errors.add(1);
        }

        sleep(randomIntBetween(2, 4));
    });
}