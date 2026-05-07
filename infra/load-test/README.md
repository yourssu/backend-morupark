# k6 Stress Test

Use a single script with environment-variable configuration:

```bash
k6 run infra/load-test/stress_test.js
```

Example override:

```bash
TARGET_VUS=1000 STEADY_DURATION=10m P95_MS=3000 k6 run infra/load-test/stress_test.js
```

Supported env vars:
- `BASE_URL`
- `TARGET_VUS`
- `RAMP_UP_DURATION`
- `STEADY_DURATION`
- `RAMP_DOWN_DURATION`
- `P95_MS`
- `FAIL_RATE`
- `STATUS_POLL_COUNT`
- `THINK_TIME_SEC`
