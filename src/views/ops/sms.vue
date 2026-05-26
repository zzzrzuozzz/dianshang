<template>
  <NotificationListPanel
    msg-type="SMS"
    add-route="/ops/sms/add"
    content-column-label="短信内容"
    :show-publish-status="true"
    :show-receive-volume="true"
    :action-width="200"
  />
</template>

<script setup>
import NotificationListPanel from '@/components/ops/NotificationListPanel.vue'
</script>
