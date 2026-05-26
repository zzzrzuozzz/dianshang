import * as echarts from 'echarts'
import { onBeforeUnmount } from 'vue'

/**
 * 管理页面内多个 ECharts 实例的初始化、resize 与销毁
 */
export function useChartGroup() {
  const instances = new Map()

  const initChart = (el, key) => {
    if (!el) return null
    if (instances.has(key)) {
      instances.get(key).dispose()
    }
    const chart = echarts.init(el)
    instances.set(key, chart)
    return chart
  }

  const setOption = (key, option, notMerge = false) => {
    const chart = instances.get(key)
    if (chart) chart.setOption(option, notMerge)
  }

  const resizeAll = () => {
    instances.forEach((chart) => chart.resize())
  }

  const disposeAll = () => {
    instances.forEach((chart) => chart.dispose())
    instances.clear()
  }

  onBeforeUnmount(disposeAll)

  return { initChart, setOption, resizeAll, disposeAll, getChart: (key) => instances.get(key) }
}
