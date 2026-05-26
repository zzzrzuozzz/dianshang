import { get } from '@/utils/request'

export interface StatCardDto {
  key: string
  label: string
  value: number
  displayValue: string
  trend: number
  compareLabel: string
  iconKey: string
  iconBg: string
  iconColor: string
}

export interface PendingTaskDto {
  key: string
  label: string
  count: number
}

export interface QuickAccessDto {
  key: string
  label: string
  path: string
  iconKey: string
  iconBg: string
  iconColor: string
}

export interface DashboardOverviewDto {
  stats: StatCardDto[]
  pendingTasks: PendingTaskDto[]
  quickAccess: QuickAccessDto[]
  chart: {
    dates: string[]
    sales: number[]
    orders: number[]
  }
}

/** GET /api/dashboard/overview */
export function fetchDashboardOverview() {
  return get<DashboardOverviewDto>('/api/dashboard/overview')
}
