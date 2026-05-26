<template>
  <div class="region-page">
    <el-card shadow="never" class="search-card">
      <el-input
        v-model="keyword"
        placeholder="输入省份名称定位检索"
        clearable
        style="width: 280px"
        @input="filterProvinces"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <span class="hint">仅加载省级数据；展开行时按 parentCode 异步拉取下级，避免一次性加载全国数据卡顿</span>
    </el-card>

    <el-card v-loading="rootLoading" shadow="never" class="table-card">
      <el-table
        :data="displayProvinces"
        row-key="code"
        border
        lazy
        :load="handleLoadChildren"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
      >
        <el-table-column prop="code" label="区划代码" width="140" />
        <el-table-column prop="name" label="区域名称" min-width="160" />
        <el-table-column label="所属层级" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.level)" size="small" effect="plain">
              {{ levelLabel(row.level) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="parentCode" label="父级代码" width="120" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onActivated } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { fetchRegionsByParent } from '@/api/region'

const keyword = ref('')
const rootLoading = ref(false)
const allProvinces = ref([])
const displayProvinces = ref([])

const levelLabel = (level) => {
  if (level === 1) return '省级'
  if (level === 2) return '市级'
  return '区县级'
}

const levelTagType = (level) => {
  if (level === 1) return 'primary'
  if (level === 2) return 'success'
  return 'warning'
}

const normalizeRow = (item) => ({
  code: item.code,
  name: item.name,
  level: item.level,
  parentCode: item.parentCode ?? '0',
  hasChildren: item.hasChildren ?? item.level < 3,
})

/** 初始仅加载全国省份/直辖市 parentCode=0 */
const loadProvinces = async () => {
  rootLoading.value = true
  try {
    const list = await fetchRegionsByParent('0')
    allProvinces.value = list.map(normalizeRow)
    filterProvinces()
  } finally {
    rootLoading.value = false
  }
}

const filterProvinces = () => {
  const kw = keyword.value.trim()
  if (!kw) {
    displayProvinces.value = [...allProvinces.value]
    return
  }
  displayProvinces.value = allProvinces.value.filter((r) => r.name.includes(kw))
}

/**
 * 行展开懒加载：用当前行 code 作为 parentCode 请求下级市/区
 */
const handleLoadChildren = async (row, _treeNode, resolve) => {
  try {
    const children = await fetchRegionsByParent(row.code)
    resolve(children.map(normalizeRow))
  } catch {
    resolve([])
  }
}

onMounted(loadProvinces)
onActivated(loadProvinces)
</script>

<style scoped>
.region-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.search-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}
.hint {
  font-size: 12px;
  color: #909399;
}
.table-card {
  border-radius: 8px;
}
</style>
