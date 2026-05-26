<template>
  <el-dialog v-model="visible" title="添加商品" width="900px" destroy-on-close @closed="onClosed">
    <el-form :inline="true" :model="searchForm" class="dialog-search">
      <el-form-item label="商品">
        <el-input v-model="searchForm.keyword" placeholder="请输入名称或编号" clearable style="width: 200px" />
      </el-form-item>
      <el-form-item>
        <el-button @click="resetSearch">重置</el-button>
        <el-button type="primary" @click="filterList">查询</el-button>
      </el-form-item>
      <el-form-item class="selected-tip">当前已选择 {{ selectedIds.length }} 件商品</el-form-item>
    </el-form>

    <el-tabs v-model="dialogTab">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane :label="`已选择 (${selectedIds.length})`" name="selected" />
    </el-tabs>

    <el-table :data="displayList" border max-height="360">
      <el-table-column prop="name" label="商品名称" min-width="200" show-overflow-tooltip />
      <el-table-column prop="id" label="商品编号" width="100" align="center" />
      <el-table-column label="商品价格" width="100" align="center">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="stock" label="剩余库存" width="100" align="center" />
      <el-table-column label="操作" width="100" align="center">
        <template #default="{ row }">
          <span v-if="isSelected(row.id)" class="picked">已选择</span>
          <el-button v-else type="primary" link @click="toggle(row)">选择</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="dialog-pagination">
      <el-pagination layout="prev, pager, next" :total="displayList.length" :page-size="10" background />
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button type="primary" @click="confirm">添加</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { pickProducts } from '@/mock/content'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  selected: { type: Array, default: () => [] },
})

const emit = defineEmits(['update:modelValue', 'confirm'])

const visible = computed({
  get: () => props.modelValue,
  set: (v) => emit('update:modelValue', v),
})

const dialogTab = ref('all')
const searchForm = reactive({ keyword: '' })
const allProducts = ref([...pickProducts])
const selectedIds = ref([...props.selected])

watch(
  () => props.selected,
  (v) => {
    selectedIds.value = [...v]
  },
  { deep: true },
)

const filtered = computed(() => {
  const kw = searchForm.keyword.trim()
  if (!kw) return allProducts.value
  return allProducts.value.filter((p) => p.name.includes(kw) || p.id.includes(kw))
})

const displayList = computed(() => {
  if (dialogTab.value === 'selected') {
    return filtered.value.filter((p) => selectedIds.value.includes(p.id))
  }
  return filtered.value
})

const isSelected = (id) => selectedIds.value.includes(id)

const toggle = (row) => {
  if (!isSelected(row.id)) selectedIds.value.push(row.id)
}

const filterList = () => {}
const resetSearch = () => {
  searchForm.keyword = ''
}

const confirm = () => {
  const items = allProducts.value.filter((p) => selectedIds.value.includes(p.id))
  emit('confirm', { ids: [...selectedIds.value], items })
  visible.value = false
}

const onClosed = () => {
  dialogTab.value = 'all'
}
</script>

<style scoped>
.dialog-search { display: flex; flex-wrap: wrap; align-items: center; }
.selected-tip { margin-left: auto; color: #606266; font-size: 13px; }
.picked { color: #67c23a; font-weight: 500; }
.dialog-pagination { margin-top: 12px; display: flex; justify-content: flex-end; }
</style>
