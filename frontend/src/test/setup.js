// Testes sempre no fuso de Brasília, para darem o mesmo resultado no computador e no CI (que roda em UTC)
process.env.TZ = 'America/Sao_Paulo';

import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterEach } from 'vitest';

afterEach(() => cleanup());
