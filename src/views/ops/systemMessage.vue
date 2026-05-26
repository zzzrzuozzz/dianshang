<template>
  <NotificationListPanel
    msg-type="SYSTEM"
    add-route="/ops/system-message/add"
    content-column-label="消息标题"
    :show-publish-status="true"
    :show-app-push="true"
    :show-msg-type="true"
    :show-click-count="true"
    :category-tabs="categoryTabs"
  />
</template>

<script setup>
import NotificationListPanel from '@/components/ops/NotificationListPanel.vue'

const categoryTabs = [
  { label: '全部', name: 'all' },
  { label: '系统消息', name: 'system' },
  { label: '客服消息', name: 'service' },
  { label: '运营消息', name: 'ops' },
]
</script>
