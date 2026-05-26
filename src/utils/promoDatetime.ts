/** 将接口返回的展示时间转为 el-date-picker 可用的格式 */
export function toPickerDatetime(value: string | undefined): string {
  if (!value) return ''
  const normalized = value.trim().replace('T', ' ')
  if (/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}$/.test(normalized)) return normalized
  const m = normalized.match(/^(\d{4})-(\d{1,2})-(\d{1,2}) (\d{1,2}):(\d{2})(?::(\d{2}))?$/)
  if (!m) return normalized
  const pad = (n: string) => n.padStart(2, '0')
  return `${m[1]}-${pad(m[2])}-${pad(m[3])} ${pad(m[4])}:${pad(m[5])}:${pad(m[6] || '0')}`
}
