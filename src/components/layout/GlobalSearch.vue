<template>
  <div class="global-search" :class="{ 'is-expanded': expanded }">
    <el-icon v-if="!expanded" class="search-trigger" @click="openSearch">
      <Search />
    </el-icon>

    <el-autocomplete
      v-else
      ref="autocompleteRef"
      v-model="keyword"
      class="search-input"
      :fetch-suggestions="querySearchAsync"
      :trigger-on-focus="false"
      :debounce="0"
      clearable
      placeholder="输入商品名称/订单号/用户手机号全局搜索..."
      popper-class="global-search-popper"
      @select="handleSelect"
      @blur="onBlur"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
      <template #default="{ item }">
        <div v-if="item.isGroup" class="suggestion-group">{{ item.label }}</div>
        <div v-else class="suggestion-item">
          <el-tag size="small" :type="tagType(item.categoryKey)" effect="plain" class="suggestion-tag">
            {{ item.category }}
          </el-tag>
          <span class="suggestion-text">{{ item.id }} - {{ item.name }}</span>
        </div>
      </template>
    </el-autocomplete>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { fetchGlobalSearch } from '@/api/globalSearch'

const router = useRouter()
const expanded = ref(false)
const keyword = ref('')
const autocompleteRef = ref(null)

let debounceTimer = null
let lastQuery = ''
let pendingCallback = null

const tagType = (key) => {
  if (key === 'product') return 'primary'
  if (key === 'order') return 'warning'
  return 'success'
}

const openSearch = async () => {
  expanded.value = true
  await nextTick()
  autocompleteRef.value?.focus?.()
}

const onBlur = () => {
  window.setTimeout(() => {
    if (!keyword.value) expanded.value = false
  }, 200)
}

/**
 * 远程搜索（300ms 防抖，避免连续输入轰炸后端）
 */
const querySearchAsync = (queryString, cb) => {
  pendingCallback = cb
  lastQuery = queryString || ''

  if (debounceTimer) clearTimeout(debounceTimer)

  if (!lastQuery.trim()) {
    cb([])
    return
  }

  debounceTimer = window.setTimeout(async () => {
    const q = lastQuery
    try {
      const suggestions = await fetchGlobalSearch(q)
      if (q === lastQuery && pendingCallback) {
        pendingCallback(suggestions)
      }
    } catch {
      if (pendingCallback) pendingCallback([])
    }
  }, 300)
}

const handleSelect = (item) => {
  if (!item || item.isGroup) return

  keyword.value = ''
  expanded.value = false

  const url = item.targetUrl || '/dashboard'
  if (url.includes('?')) {
    const [path, query] = url.split('?')
    const params = Object.fromEntries(new URLSearchParams(query))
    router.push({ path, query: params })
  } else {
    router.push(url)
  }
}
</script>

<style scoped>
.global-search {
  display: flex;
  align-items: center;
}

.global-search.is-expanded {
  width: 320px;
}

.search-trigger {
  font-size: 18px;
  color: #606266;
  cursor: pointer;
}

.search-trigger:hover {
  color: #409eff;
}

.search-input {
  width: 100%;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 20px;
}

.suggestion-group {
  font-size: 12px;
  font-weight: 600;
  color: #909399;
  padding: 6px 0 2px;
  pointer-events: none;
}

.suggestion-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  line-height: 1.4;
}

.suggestion-tag {
  flex-shrink: 0;
}

.suggestion-text {
  font-size: 13px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>

<style>
.global-search-popper .el-autocomplete-suggestion__wrap {
  max-height: 360px;
}

.global-search-popper li {
  line-height: normal;
}
</style>
