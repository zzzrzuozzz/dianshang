<template>
  <div v-loading="loading" class="topic-detail-page">
    <el-card shadow="never" class="panel-card preview-card">
      <template #header>
        <span class="section-dot" /> 专题详情
      </template>
      <div class="preview-gallery">
        <el-image v-for="(img, i) in detail.images" :key="i" :src="img" fit="cover" class="gallery-img" />
      </div>
      <h2 class="preview-title">{{ detail.title }}</h2>
      <p class="preview-content">{{ detail.content }}</p>
      <div class="preview-stats">
        <span>收藏：{{ detail.collectCount }}</span>
        <span>阅读：{{ detail.readCount }}</span>
        <span>转发：{{ detail.shareCount }}</span>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <template #header><span class="section-title">关联商品</span></template>
      <div class="product-row">
        <div v-for="p in detail.products" :key="p.id" class="product-card">
          <el-image :src="p.thumb" fit="cover" class="product-thumb" />
          <p class="product-name">{{ p.name }}</p>
          <p class="product-price">¥{{ p.price }}</p>
        </div>
      </div>
    </el-card>

    <el-card shadow="never" class="panel-card">
      <div class="comment-toolbar">
        <el-tabs v-model="commentTab">
          <el-tab-pane :label="`全部(${comments.length})`" name="all" />
          <el-tab-pane label="评论" name="comment" />
        </el-tabs>
        <el-button type="danger" @click="batchDelete">批量删除</el-button>
      </div>

      <el-table :data="filteredComments" border stripe @selection-change="(r) => (selected = r)">
        <el-table-column type="selection" width="48" align="center" />
        <el-table-column prop="id" label="评价编号" width="100" align="center" />
        <el-table-column label="评价内容" min-width="220">
          <template #default="{ row }">
            <p class="comment-text">{{ row.content }}</p>
            <div v-if="row.pics?.length" class="comment-pics">
              <el-image v-for="(pic, i) in row.pics" :key="i" :src="pic" class="pic-thumb" fit="cover" />
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="replyContent" label="回复内容" min-width="160" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="openReply(row)">回复</el-button>
            <el-button link @click="toggleHide(row)">{{ row.status === 2 ? '展示' : '隐藏' }}</el-button>
            <el-button type="success" link @click="toggleFeatured(row)">
              {{ row.status === 1 ? '取消加精' : '加精' }}
            </el-button>
            <el-button type="danger" link @click="deleteComment(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-bar">
        <span>第1页 共10页 {{ comments.length }}条</span>
        <el-pagination :total="comments.length" layout="prev, pager, next, sizes" background />
      </div>
    </el-card>

    <el-dialog v-model="replyVisible" title="回复评论" width="480px">
      <el-input v-model="replyText" type="textarea" :rows="4" placeholder="请输入回复内容" />
      <template #footer>
        <el-button @click="replyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { mockTopicDetail, mockTopicComments } from '@/mock/content'

const route = useRoute()
const loading = ref(false)
const commentTab = ref('all')
const comments = ref([])
const selected = ref([])
const detail = reactive({ ...mockTopicDetail })
const replyVisible = ref(false)
const replyText = ref('')
const currentRow = ref(null)

const filteredComments = computed(() => comments.value)

const statusTagType = (status) => {
  if (status === 1) return 'success'
  if (status === 2) return 'info'
  return 'warning'
}

/**
 * GET /api/content/topic/detail/{id}
 * POST /api/content/topic/comment/review — 加精/隐藏等审核操作
 */
const loadTopicDetailPage = async () => {
  loading.value = true
  try {
    await new Promise((r) => setTimeout(r, 400))
    const id = route.params.id
    console.log('[mock] load topic detail', id)
    Object.assign(detail, mockTopicDetail)
    comments.value = mockTopicComments.map((c) => ({ ...c }))
  } finally {
    loading.value = false
  }
}

const openReply = (row) => {
  currentRow.value = row
  replyText.value = row.replyContent || ''
  replyVisible.value = true
}

const submitReply = async () => {
  if (currentRow.value) {
    currentRow.value.replyContent = replyText.value
    await new Promise((r) => setTimeout(r, 300))
    ElMessage.success('回复已保存')
  }
  replyVisible.value = false
}

const toggleHide = async (row) => {
  row.status = row.status === 2 ? 0 : 2
  row.statusText = row.status === 2 ? '已隐藏' : '待审核'
  await reviewComment(row, 'hide')
}

const toggleFeatured = async (row) => {
  row.status = row.status === 1 ? 0 : 1
  row.statusText = row.status === 1 ? '已加精' : '待审核'
  await reviewComment(row, 'feature')
}

/** POST /api/content/topic/comment/review */
const reviewComment = async (row, action) => {
  await new Promise((r) => setTimeout(r, 200))
  console.log('[mock] comment review', { id: row.id, action })
  ElMessage.success('操作成功')
}

const deleteComment = (row) => {
  ElMessageBox.confirm('确定删除该评论吗？', '提示', { type: 'warning' })
    .then(() => {
      comments.value = comments.value.filter((c) => c.id !== row.id)
      ElMessage.success('已删除')
    })
    .catch(() => {})
}

const batchDelete = () => {
  if (!selected.value.length) {
    ElMessage.warning('请先选择评论')
    return
  }
  ElMessageBox.confirm(`确定删除选中的 ${selected.value.length} 条评论吗？`, '提示', { type: 'warning' })
    .then(() => ElMessage.success('批量删除成功'))
    .catch(() => {})
}

onMounted(loadTopicDetailPage)
</script>

<style scoped>
.topic-detail-page { min-height: calc(100vh - 120px); }
.panel-card { margin-bottom: 12px; border-radius: 8px; border: none; }
.section-dot::before { content: ''; display: inline-block; width: 8px; height: 8px; background: #409eff; border-radius: 50%; margin-right: 8px; }
.preview-gallery { display: flex; gap: 12px; margin-bottom: 16px; }
.gallery-img { width: 120px; height: 120px; border-radius: 4px; }
.preview-title { font-size: 18px; margin: 0 0 12px; color: #303133; }
.preview-content { line-height: 1.8; color: #606266; margin-bottom: 16px; }
.preview-stats { text-align: right; font-size: 13px; color: #909399; display: flex; gap: 16px; justify-content: flex-end; }
.product-row { display: flex; gap: 16px; flex-wrap: wrap; }
.product-card { width: 140px; }
.product-thumb { width: 140px; height: 140px; border-radius: 4px; }
.product-name { font-size: 13px; margin: 8px 0 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.product-price { color: #f56c6c; font-weight: 600; margin: 0; }
.comment-toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.comment-text { margin: 0 0 8px; }
.comment-pics { display: flex; gap: 6px; }
.pic-thumb { width: 48px; height: 48px; border-radius: 4px; }
.pagination-bar { display: flex; justify-content: space-between; margin-top: 16px; font-size: 13px; color: #606266; }
</style>
