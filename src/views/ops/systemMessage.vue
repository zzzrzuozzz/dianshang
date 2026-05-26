<template>
  <div>
    <NoticeScopeHelp title="运营推送说明" />
    <NotificationListPanel
    msg-type="SYSTEM"
    add-route="/ops/system-message/add"
    content-column-label="消息标题"
    :show-publish-status="true"
    :show-app-push="true"
    :show-msg-type="true"
    :show-click-count="true"
    :show-presets="true"
    :category-tabs="categoryTabs"
    />
  </div>
</template>

<script setup>
import NoticeScopeHelp from '@/components/common/NoticeScopeHelp.vue'
import NotificationListPanel from '@/components/ops/NotificationListPanel.vue'

const categoryTabs = [
  { label: '全部', name: 'all' },
  { label: '系统消息', name: 'system' },
  { label: '客服消息', name: 'service' },
  { label: '运营消息', name: 'ops' },
]
</script>
