/**
 * 从 modood/Administrative-divisions-of-China 生成完整国标 sys_region SQL。
 *
 * 用法（项目根目录或 backend 目录）：
 *   node backend/scripts/generate-region-sql.mjs
 *
 * 输出：
 *   backend/src/main/resources/region/region-full.sql
 */
import { writeFileSync, mkdirSync, readFileSync, existsSync } from 'node:fs'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const OUT_DIR = join(__dirname, '../src/main/resources/region')
const OUT_FILE = join(OUT_DIR, 'region-full.sql')
const DATA_DIR = join(__dirname, 'data')
const BASE =
  'https://raw.githubusercontent.com/modood/Administrative-divisions-of-China/master/dist'

function padCode(code, len = 6) {
  return String(code).padEnd(len, '0').slice(0, len)
}

function provinceCode(code) {
  return padCode(code, 6)
}

function cityCode(code) {
  const s = String(code)
  if (s.length >= 6) return s.slice(0, 6)
  return padCode(s, 6)
}

function areaCode(code) {
  const s = String(code)
  if (s.length >= 6) return s.slice(0, 6)
  return padCode(s, 6)
}

function esc(str) {
  return String(str).replace(/'/g, "''")
}

async function loadJson(name) {
  const local = join(DATA_DIR, name)
  if (existsSync(local)) {
    return JSON.parse(readFileSync(local, 'utf8'))
  }
  const res = await fetch(`${BASE}/${name}`)
  if (!res.ok) throw new Error(`Failed to fetch ${name}: ${res.status}`)
  return res.json()
}

async function main() {
  console.log('Loading provinces, cities, areas...')
  const [provinces, cities, areas] = await Promise.all([
    loadJson('provinces.json'),
    loadJson('cities.json'),
    loadJson('areas.json'),
  ])

  const rows = []
  const push = (code, name, parentCode, level) => {
    rows.push(`('${esc(code)}','${esc(name)}','${esc(parentCode)}',${level})`)
  }

  for (const p of provinces) {
    const code = provinceCode(p.code)
    push(code, p.name, '0', 1)
  }

  for (const c of cities) {
    const code = cityCode(c.code)
    const parent = provinceCode(c.provinceCode)
    push(code, c.name, parent, 2)
  }

  for (const a of areas) {
    const code = areaCode(a.code)
    const parent = cityCode(a.cityCode)
    push(code, a.name, parent, 3)
  }

  mkdirSync(OUT_DIR, { recursive: true })

  const header = `-- 国标省市区完整数据（modood/Administrative-divisions-of-China）
-- 生成时间: ${new Date().toISOString()}
-- 记录数: ${rows.length}
DELETE FROM sys_region;

`

  const chunks = []
  const BATCH = 400
  for (let i = 0; i < rows.length; i += BATCH) {
    const part = rows.slice(i, i + BATCH).join(',\n')
    chunks.push(
      `INSERT INTO sys_region (code, name, parent_code, level_num) VALUES\n${part};`,
    )
  }

  writeFileSync(OUT_FILE, header + chunks.join('\n\n') + '\n', 'utf8')
  console.log(`Written ${rows.length} regions -> ${OUT_FILE}`)
}

main().catch((err) => {
  console.error(err)
  process.exit(1)
})
