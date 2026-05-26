export const auditStatusMap: Record<string, { label: string; type: string }> = {
  pending: { label: '待审核', type: 'warning' },
  passed: { label: '已通过', type: 'success' },
  rejected: { label: '未通过', type: 'danger' },
}
